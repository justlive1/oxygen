/*
 * Copyright (C) 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package vip.justlive.oxygen.core.aop.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.bean.SimpleBeanProxy;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.util.base.ClassUtils;
import vip.justlive.oxygen.core.util.base.MoreObjects;
import vip.justlive.oxygen.core.util.base.Strings;
import vip.justlive.oxygen.core.util.base.TypeImpl;
import vip.justlive.oxygen.core.util.compiler.DynamicCompiler;
import vip.justlive.oxygen.core.util.io.IoUtils;
import vip.justlive.oxygen.core.util.template.Templates;

/**
 * 使用jdk动态编译的代理
 *
 * @author wubo
 */
@Slf4j
public class CompilerBeanProxy extends SimpleBeanProxy {

  private static final AtomicLong ID = new AtomicLong();
  private static final Map<Long, Method> METHODS = new HashMap<>(16, 1f);
  private static final String CODE_TEMPLATE;
  private final DynamicCompiler compiler = new DynamicCompiler();

  static {
    try (InputStream in = CompilerBeanProxy.class.getResourceAsStream("/enhancer.tpl")) {
      CODE_TEMPLATE = IoUtils.toString(in);
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

  public static Method lookup(long key) {
    return METHODS.get(key);
  }

  @Override
  public <T> T proxy(Class<T> clazz, Object... args) {
    ClassMeta classMeta = new ClassMeta();
    Class<?> actualClass = ClassUtils.getActualClass(clazz);
    classMeta.packageName = actualClass.getPackage().getName();
    classMeta.className = actualClass.getSimpleName() + "$$EnhancerByOxygen";
    classMeta.targetClass = ClassUtils.getClassName(actualClass);
    getTypeVars(actualClass, classMeta.typeVars);
    classMeta.generic = getGeneric(actualClass.getTypeParameters(), true);
    classMeta.targetGeneric = getGeneric(actualClass.getTypeParameters(), false);
    classMeta.constructors = getConstructorMeta(actualClass, classMeta);
    classMeta.methods = getMethodMeta(actualClass, classMeta);

    if (classMeta.methods.isEmpty()) {
      return super.proxy(clazz, args);
    }
    String code = Templates.render(CODE_TEMPLATE, MoreObjects.beanToMap(classMeta));
    if (log.isTraceEnabled()) {
      log.trace("Generate proxy class {}.{}: \n{}", classMeta.packageName, classMeta.className,
          code);
    }
    Class<?> proxyClass = compiler.compile(classMeta.packageName, classMeta.className, code);
    @SuppressWarnings("unchecked")
    T bean = (T) super.proxy(proxyClass, args);
    ProxyStore.PROXIES.add(bean);
    return bean;
  }

  private void getTypeVars(Class<?> clazz, Map<Class<?>, Map<TypeVariable<?>, Type>> typeVars) {
    Class<?> type = clazz;
    while (type != null && type != Object.class) {
      Type gsc = type.getGenericSuperclass();
      type = type.getSuperclass();
      if (!(gsc instanceof ParameterizedType)) {
        continue;
      }
      Map<TypeVariable<?>, Type> map = typeVars.computeIfAbsent(type, k -> new HashMap<>(4));
      ParameterizedType pt = (ParameterizedType) gsc;
      Type[] ats = pt.getActualTypeArguments();
      TypeVariable<?>[] tps = type.getTypeParameters();
      for (int i = 0; i < tps.length; i++) {
        map.put(tps[i], ats[i]);
      }
    }
  }

  private List<ConstructorMeta> getConstructorMeta(Class<?> actualClass, ClassMeta classMeta) {
    List<ConstructorMeta> constructorMetas = new ArrayList<>();
    for (Constructor<?> constructor : actualClass.getConstructors()) {
      ConstructorMeta meta = new ConstructorMeta();
      meta.params = getGeneric(constructor.getParameters(), actualClass, classMeta);
      meta.exceptions = getExceptionTypes(constructor.getExceptionTypes());
      constructorMetas.add(meta);
    }
    return constructorMetas;
  }

  private List<MethodMeta> getMethodMeta(Class<?> actualClass, ClassMeta classMeta) {
    List<MethodMeta> methodMetas = new ArrayList<>();
    for (Method method : actualClass.getMethods()) {
      if (skip(method)) {
        continue;
      }
      MethodMeta meta = new MethodMeta();
      meta.key = ID.getAndIncrement();
      meta.method = method;
      meta.name = method.getName();
      meta.generic = getGeneric(method.getTypeParameters(), true);
      meta.returnType = getReturnType(method);
      meta.exceptions = getExceptionTypes(method.getExceptionTypes());
      meta.params = getGeneric(method.getParameters(), method.getDeclaringClass(), classMeta);
      METHODS.put(meta.key, method);
      methodMetas.add(meta);
    }
    return methodMetas;
  }

  private String getReturnType(Method method) {
    return new TypeImpl(method.getGenericReturnType()).getTypeName();
  }

  private String[] getExceptionTypes(Class<?>[] types) {
    String[] rts = new String[types.length];
    for (int i = 0; i < types.length; i++) {
      rts[i] = ClassUtils.getClassName(types[i]);
    }
    return rts;
  }

  private String[] getGeneric(Parameter[] params, Class<?> clazz, ClassMeta classMeta) {
    String[] metaParams = new String[params.length];
    for (int i = 0; i < params.length; i++) {
      Type type = params[i].getParameterizedType();
      if (type instanceof TypeVariable<?>) {
        Map<TypeVariable<?>, Type> map = classMeta.typeVars.get(clazz);
        if (map != null && map.containsKey(type)) {
          type = map.get(type);
        }
      }
      String name = new TypeImpl(type).getTypeName();
      metaParams[i] = name;
      if (params[i].isVarArgs() && name.endsWith("[]")) {
        metaParams[i] = name.substring(0, name.length() - 2) + "...";
      }
    }
    return metaParams;
  }

  private String getGeneric(TypeVariable<?>[] types, boolean bound) {
    if (types == null || types.length == 0) {
      return Strings.EMPTY;
    }
    StringBuilder sb = new StringBuilder("<");
    for (int i = 0; i < types.length; i++) {
      TypeVariable<?> type = types[i];
      sb.append(type.getName());
      if (bound) {
        boundType(type, sb);
      }
      if (i < types.length - 1) {
        sb.append(", ");
      }
    }
    sb.append('>');
    return sb.toString();
  }

  private void boundType(TypeVariable<?> type, StringBuilder sb) {
    Type[] bounds = type.getBounds();
    if (bounds.length == 1 && bounds[0] == Object.class) {
      return;
    }
    for (int j = 0; j < bounds.length; j++) {
      if (j == 0) {
        sb.append(" extends ");
      } else {
        sb.append(" & ");
      }
      sb.append(new TypeImpl(bounds[j]).getTypeName());
    }
  }

  private boolean skip(Method method) {
    int mod = method.getModifiers();
    return Modifier.isFinal(mod) || Modifier.isStatic(mod) || !Modifier.isPublic(mod)
        || method.isBridge() || method.getDeclaringClass() == Object.class;
  }

  @Getter
  public static class ClassMeta {

    private String packageName;
    private String className;
    private String generic;
    private String targetClass;
    private String targetGeneric;
    private List<ConstructorMeta> constructors;
    private List<MethodMeta> methods;
    private final Map<Class<?>, Map<TypeVariable<?>, Type>> typeVars = new HashMap<>();
  }

  @Getter
  public static class ConstructorMeta {

    private String[] params;
    private String[] exceptions;
  }

  @Getter
  public static class MethodMeta {

    private long key;
    private Method method;
    private String name;
    private String generic;
    private String returnType;
    private String[] params;
    private String[] exceptions;
  }
}
