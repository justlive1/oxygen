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

import java.util.Arrays;
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
    CoreConf conf = new CoreConf();
    conf.setBaseTempDir("/aaa");
    attrs.put("c", conf);
    attrs.put("b", Arrays.asList(1, 2, 3));

    String template = "<i>${a}</i><i>${1+3}</i><i>${c.baseTempDir}</i>";

    SimpleTemplateEngine engine = new SimpleTemplateEngine();
    String result = engine.render(template, attrs);
    Assert.assertEquals("<i>1</i><i>4</i><i>/aaa</i>", result);

    template = "<a>${x,$</a>";
    result = engine.render(template, attrs);
    Assert.assertEquals(template, result);

    template = "<a>${x,${a}</a>";
    result = engine.render(template, attrs);
    Assert.assertEquals("<a>${x,1</a>", result);

    template = "#: if(a == 1) { \n 1+2${a}x${a} \n #:}";
    result = engine.render(template, attrs);
    Assert.assertEquals("1+21x1", result);

    template = "#: for(var it in b) { \n ${b[it]} \n #:}";
    result = engine.render(template, attrs);
    Assert.assertEquals("123", result);
  }
}
