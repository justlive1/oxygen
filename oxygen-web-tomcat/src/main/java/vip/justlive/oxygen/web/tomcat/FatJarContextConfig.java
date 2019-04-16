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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.ContextConfig;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.apache.tomcat.Jar;
import org.apache.tomcat.util.descriptor.web.WebXml;
import org.apache.tomcat.util.scan.JarFactory;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * fat-jar for tomcat context config
 *
 * @author wubo
 */
public class FatJarContextConfig extends ContextConfig {

  private static final Log log = LogFactory.getLog(FatJarContextConfig.class);

  @Override
  protected void processResourceJARs(Set<WebXml> fragments) {
    for (WebXml fragment : fragments) {
      URL url = fragment.getURL();
      try {
        String urlString = url.toString();
        if (urlString.indexOf(Constants.JAR_URL_SEPARATOR) < urlString
            .lastIndexOf(Constants.JAR_URL_SEPARATOR)) {
          urlString = urlString.substring(0, urlString.length() - 2);
        }
        url = new URL(urlString);
        if (Constants.URL_PROTOCOL_JAR.equals(url.getProtocol()) || url.toString()
            .endsWith(Constants.JAR_EXT)) {
          processJar(url);
        } else if (Constants.URL_PROTOCOL_FILE.equals(url.getProtocol())) {
          File file = new File(url.toURI());
          File resources = new File(file, Constants.META_INF_RESOURCES);
          if (resources.isDirectory()) {
            context.getResources()
                .createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR,
                    Constants.ROOT_PATH, resources.getAbsolutePath(), null, Constants.ROOT_PATH);
          }
        }
      } catch (IOException | URISyntaxException e) {
        log.error(sm.getString("contextConfig.resourceJarFail", url, context.getName()));
      }
    }
  }

  private void processJar(URL url) throws IOException {
    try (Jar jar = JarFactory.newInstance(url)) {
      jar.nextEntry();
      String entryName = jar.getEntryName();
      while (entryName != null) {
        if (entryName.startsWith(Constants.META_INF_RESOURCES)) {
          context.getResources().createWebResourceSet(WebResourceRoot.ResourceSetType.RESOURCE_JAR,
              Constants.ROOT_PATH, url, Constants.META_INF_RESOURCES_PATH);
          break;
        }
        jar.nextEntry();
        entryName = jar.getEntryName();
      }
    }
  }
}
