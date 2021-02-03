/*
 * Copyright (C) 2020 the original author or authors.
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
package vip.justlive.oxygen.core.util.template;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * 模板引擎
 *
 * @author wubo
 */
public interface TemplateEngine {

  /**
   * 渲染模板
   *
   * @param template 模板
   * @param attrs 属性
   * @return result
   */
  String render(String template, Map<String, Object> attrs);

  /**
   * 渲染模板
   *
   * @param template 模板
   * @param attrs 属性
   * @param writer 输出
   */
  default void render(String template, Map<String, Object> attrs, Writer writer) {
    try {
      writer.append(render(template, attrs));
    } catch (IOException e) {
      throw Exceptions.wrap(e);
    }
  }

}
