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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import vip.justlive.oxygen.core.util.ExpressionUtils;

/**
 * 简单模板引擎实现
 *
 * @author wubo
 */
public class SimpleTemplateEngine implements TemplateEngine {

  private static final Pattern REGEX = Pattern.compile("[$][{]([^{}]*)[}]");
  private static final String AT = "____AT____";
  private static final Map<String, TemplateSpec> CACHE = new ConcurrentHashMap<>(4);


  @Override
  public String render(String template, Map<String, Object> attrs) {
    TemplateSpec spec = CACHE.get(template);
    if (spec != null) {
      return spec.apply(attrs);
    }
    spec = new TemplateSpec(template);
    CACHE.put(template, spec);
    return spec.apply(attrs);
  }

  private class TemplateSpec implements Function<Map<String, Object>, String> {

    private final String[] data;
    private final List<String> selectors;

    TemplateSpec(String template) {
      Matcher matcher = REGEX.matcher(template);
      int index = 0;
      String value = template;
      selectors = new LinkedList<>();
      while (matcher.find(index)) {
        selectors.add(matcher.group(1).trim());
        value = value.replace(matcher.group(0), AT);
        index = matcher.end();
      }
      this.data = value.split(AT);
    }

    @Override
    public String apply(Map<String, Object> attrs) {
      StringBuilder sb = new StringBuilder(data[0]);
      for (int i = 0; i < data.length - 1; i++) {
        sb.append(ExpressionUtils.eval(attrs, selectors.get(i)));
        sb.append(data[i + 1]);
      }
      return sb.toString();
    }
  }
}
