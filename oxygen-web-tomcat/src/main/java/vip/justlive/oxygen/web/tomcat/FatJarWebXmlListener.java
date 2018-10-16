/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.web.tomcat;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.catalina.Context;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.WebResourceRoot.ResourceSetType;
import org.apache.catalina.webresources.StandardRoot;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * fat-jar加载classpath下的WEB-INF
 *
 * @author wubo
 */
public class FatJarWebXmlListener implements LifecycleListener {

  @Override
  public void lifecycleEvent(LifecycleEvent event) {
    if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
      Context context = (Context) event.getLifecycle();
      WebResourceRoot resources = context.getResources();
      if (resources == null) {
        resources = new StandardRoot(context);
        context.setResources(resources);
      }

      // 使用embedded tomcat时 WEB-INF放在了classpath下
      URL resource = context.getParentClassLoader().getResource(Constants.WEB_INF);
      if (resource != null) {
        String webXmlUrlString = resource.toString();
        try {
          URL root = new URL(
              webXmlUrlString.substring(0, webXmlUrlString.length() - Constants.WEB_INF.length()));
          resources.createWebResourceSet(ResourceSetType.RESOURCE_JAR, Constants.WEB_INF_PATH, root,
              Constants.WEB_INF_PATH);
        } catch (MalformedURLException e) {
          // ignore
        }
      }
    }
  }
}
