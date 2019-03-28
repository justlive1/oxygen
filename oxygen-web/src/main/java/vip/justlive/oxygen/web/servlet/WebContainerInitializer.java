/*
 *  Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
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
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 容器自动初始化
 * <br>
 * 应用于部署在外部容器，eg: jetty,tomcat
 * <br>
 * 先启动Servlet服务器,服务器启动应用(WebContainerInitializer),然后启动IOC容器
 *
 * @author wubo
 */
@HandlesTypes(WebAppInitializer.class)
public class WebContainerInitializer implements ServletContainerInitializer {

  @Override
  public void onStartup(Set<Class<?>> classes, ServletContext ctx) {
    List<WebAppInitializer> initializers = new LinkedList<>();
    if (classes != null) {
      try {
        for (Class<?> clazz : classes) {
          if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())
              && WebAppInitializer.class.isAssignableFrom(clazz)) {
            initializers.add((WebAppInitializer) clazz.newInstance());
          }
        }
      } catch (InstantiationException | IllegalAccessException e) {
        throw Exceptions.wrap(e);
      }
    }

    if (initializers.isEmpty()) {
      // 兼容一些jetty版本扫描不到jar中实现
      ctx.log("No WebAppInitializer types detected on classpath and add DefaultWebAppInitializer");
      initializers.add(new DefaultWebAppInitializer());
    }

    Collections.sort(initializers);

    ctx.log(initializers.size() + " WebAppInitializer detected on classpath");
    for (WebAppInitializer initializer : initializers) {
      initializer.onStartup(ctx);
    }
  }

}
