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

package vip.justlive.oxygen.core.util.json;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.experimental.UtilityClass;
import vip.justlive.oxygen.core.exception.Exceptions;

/**
 * json
 *
 * @author wubo
 */
@UtilityClass
public class Json {

  private final JsonWriter DEFAULT_WRITER = new JsonWriter();
  private final AtomicReference<Writer> WRITER = new AtomicReference<>();
  private final AtomicReference<Reader> READER = new AtomicReference<>();

  /**
   * 获取json writer
   *
   * @return writer
   */
  public Writer getWriter() {
    return WRITER.get();
  }

  /**
   * 设置json writer
   *
   * @param writer writer
   */
  public void setWriter(Writer writer) {
    WRITER.set(writer);
  }

  /**
   * 获取json reader
   *
   * @return reader
   */
  public Reader getReader() {
    return READER.get();
  }

  /**
   * 设置json reader
   *
   * @param reader reader
   */
  public void setReader(Reader reader) {
    READER.set(reader);
  }

  /**
   * object to string
   *
   * @param value object
   * @return string
   */
  public String toJson(Object value) {
    return Optional.ofNullable(getWriter()).orElse(DEFAULT_WRITER).toJson(value);
  }

  /**
   * json string转对象
   *
   * @param json json string
   * @param type 类型
   * @param <T>  泛型
   * @return 对象
   */
  public <T> T parse(String json, Type type) {
    Reader reader = getReader();
    if (reader == null) {
      throw Exceptions.fail("json reader is unavailable");
    }
    return reader.parse(json, type);
  }

  /**
   * json绑定
   *
   * @param data 对象
   * @param type 类型
   * @param <T>  泛型
   * @return 转换后的对象
   */
  public <T> T bindTo(Object data, Type type) {
    Reader reader = getReader();
    if (reader == null) {
      throw Exceptions.fail("json reader is unavailable");
    }
    return reader.bindTo(data, type);
  }

}
