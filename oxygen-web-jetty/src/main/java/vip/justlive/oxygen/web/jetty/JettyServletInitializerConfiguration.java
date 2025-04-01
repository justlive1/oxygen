/*
 * Copyright (C) 2025 the original author or authors.
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

package vip.justlive.oxygen.web.jetty;

import org.eclipse.jetty.ee10.webapp.AbstractConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import vip.justlive.oxygen.web.servlet.ServletWebInitializer;

/**
 * jetty servlet initializer configuration
 *
 * @author wu-bo
 */
public class JettyServletInitializerConfiguration extends AbstractConfiguration {

  protected JettyServletInitializerConfiguration() {
    super(new AbstractConfiguration.Builder());
  }

  @Override
  public void configure(WebAppContext context) {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    Thread.currentThread().setContextClassLoader(context.getClassLoader());
    try {
      callInitializers(context);
    } finally {
      Thread.currentThread().setContextClassLoader(classLoader);
    }
  }

  private void callInitializers(WebAppContext context) {
    try {
      context.getContext().setExtendedListenerTypes(true);
      ServletWebInitializer initializer = new ServletWebInitializer();
      initializer.onStartup(null, context.getServletContext());
    } finally {
      context.getContext().setExtendedListenerTypes(false);
    }
  }
}
