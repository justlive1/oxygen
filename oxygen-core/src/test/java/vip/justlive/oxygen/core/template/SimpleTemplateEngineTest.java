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
import vip.justlive.oxygen.core.util.MoreObjects;

/**
 * @author wubo
 */
public class SimpleTemplateEngineTest {

  SimpleTemplateEngine engine = new SimpleTemplateEngine();

  @Test
  public void t0() {

    Map<String, Object> attrs = new HashMap<>();
    attrs.put("a", 1);
    CoreConf conf = new CoreConf();
    conf.setConfigOverridePath("/aaa");
    attrs.put("c", conf);
    attrs.put("b", Arrays.asList(1, 2, 3));

    String template = "<i>${a + 1}</i><i>${1+3}</i><i>${c.configOverridePath}</i>";

    String result = engine.render(template, attrs);
    Assert.assertEquals("<i>2</i><i>4</i><i>/aaa</i>", result);

    template = "<a>${x,$</a>";
    result = engine.render(template, attrs);
    Assert.assertEquals(template, result);

    template = "<a>\\${x,${a}</a>";
    result = engine.render(template, attrs);
    Assert.assertEquals("<a>${x,1</a>", result);


  }

  @Test
  public void t1() {
    Map<String, Object> attrs = new HashMap<>();
    attrs.put("a", 1);
    CoreConf conf = new CoreConf();
    attrs.put("c", conf);
    attrs.put("b", Arrays.asList(1, 2, 3));

    String template = "#{ if(a == 1) { } 1+2${a}x${a}  #{ \\}}";
    String result = engine.render(template, attrs);
    Assert.assertEquals(" 1+21x1  ", result);

    template = "#{ for(var it in b) { } ${b[it]} #{ \\}}";
    result = engine.render(template, attrs);
    Assert.assertEquals(" 1  2  3 ", result);
  }

  @Test
  public void t2() {
    String template = Templates.template("classpath:template/tpl.txt");
    String value = engine.render(template, MoreObjects.mapOf("a", 1, "b", new int[]{5, 9, 7}));
    Assert.assertEquals("a = 1;b[0]=5;b[1]=9;b[2]=7;", value);
  }

  @Test
  public void t3() {
    Map<String, Object> attrs = new HashMap<>();
    attrs.put("a", 1);
    attrs.put("b", "text");

    String template = "a + 2 = ${a + 2}; b = ${b.substring(1)};";

    String value = engine.render(template, attrs);
    Assert.assertEquals("a + 2 = 3; b = ext;", value);
  }
}
