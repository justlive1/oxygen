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
package vip.justlive.oxygen.web.view;

import java.util.Locale;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;
import vip.justlive.oxygen.web.view.ThymeleafHandler.ResourceTemplateResolver;

/**
 * @author wubo
 */
public class ThymeleafViewResolverTest {

  @Test
  public void test() {

    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(new ResourceTemplateResolver("/", "/tmp"));
    String rsp = templateEngine.process("template/index.html", new IContext() {
      @Override
      public Locale getLocale() {
        return Locale.getDefault();
      }

      @Override
      public boolean containsVariable(String name) {
        return false;
      }

      @Override
      public Set<String> getVariableNames() {
        return null;
      }

      @Override
      public Object getVariable(String name) {
        return null;
      }
    });
    Assert.assertEquals("xxx", rsp);
  }
}