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
package vip.justlive.oxygen.core.template;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import vip.justlive.oxygen.core.config.CoreConf;

/**
 * @author wubo
 */
public class SimpleTemplateEngineTest {

  @Test
  public void render() {

    Map<String, Object> attrs = new HashMap<>();
    attrs.put("a", 1);
    attrs.put("b", "b");
    CoreConf conf = new CoreConf();
    conf.setBaseTempDir("/aaa");
    attrs.put("c", conf);

    String template = "<i>${a}</i><i>${b}</i><i>${c.baseTempDir}</i>";

    SimpleTemplateEngine engine = new SimpleTemplateEngine();
    String result = engine.render(template, attrs);
    Assert.assertEquals("<i>1</i><i>b</i><i>/aaa</i>", result);
  }
}
