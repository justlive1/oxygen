/*
 * Copyright (C) 2019 the original author or authors.
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
package vip.justlive.oxygen.web.servlet;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.annotation.HandlesTypes;
import vip.justlive.oxygen.core.scan.ClassScannerPlugin;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * servlet容器自动初始化
 * <br>
 * 应用于部署在外部容器，eg: jetty,tomcat
 * <br>
 * 先启动Servlet服务器,服务器启动应用(ServletWebInitializer),然后启动IOC容器
 *
 * @author wubo
 */
@HandlesTypes(WebAppInitializer.class)
public class ServletWebInitializer implements ServletContainerInitializer {

  @Override
  public void onStartup(Set<Class<?>> classes, ServletContext ctx) {

    List<WebAppInitializer> initializers = new LinkedList<>();
    scan(classes, initializers);

    if (initializers.isEmpty()) {
      ctx.log("No WebAppInitializer types detected by servlet server, scan by class scanner");
      scan(ClassScannerPlugin.getClasses(), initializers);
    }

    Collections.sort(initializers);

    ctx.log(initializers.size() + " WebAppInitializer detected on classpath");
    for (WebAppInitializer initializer : initializers) {
      initializer.onStartup(ctx);
    }
  }

  private void scan(Set<Class<?>> classes, List<WebAppInitializer> initializers) {
    if (classes != null) {
      for (Class<?> clazz : classes) {
        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())
            && WebAppInitializer.class.isAssignableFrom(clazz)) {
          initializers.add((WebAppInitializer) ClassUtils.newInstance(clazz));
        }
      }
    }
  }
}
