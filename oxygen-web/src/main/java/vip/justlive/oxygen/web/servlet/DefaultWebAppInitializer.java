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

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration.Dynamic;
import vip.justlive.oxygen.core.Bootstrap;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * web启动器
 *
 * @author wubo
 */
public class DefaultWebAppInitializer implements WebAppInitializer {

  @Override
  public void onStartup(ServletContext context) {

    Bootstrap.start();

    Dynamic dynamic = context
        .addServlet(DispatcherServlet.class.getSimpleName(), DispatcherServlet.class);
    dynamic.setLoadOnStartup(0);
    dynamic.addMapping(Constants.ROOT_PATH);
    dynamic.setAsyncSupported(true);
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }
}
