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
package vip.justlive.oxygen.web.http;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * session
 *
 * @author wubo
 */
@Getter
public class Session {

  private String id;
  @Setter
  private long expired;
  private Map<String, Object> data = new HashMap<>(4);
  private transient boolean change;
  private transient SessionManager manager;

  /**
   * 重新设置sessionId
   *
   * @param id sessionId
   */
  public void setId(String id) {
    if (id != null && manager != null) {
      manager.removeSession(this.id);
    }
    this.id = id;
    if (manager != null) {
      manager.createSession(this);
    }
  }

  /**
   * 使session失效
   */
  public void expire() {
    this.expired = 0;
    if (manager != null) {
      manager.removeSession(this.id);
    }
  }

  /**
   * 添加属性
   *
   * @param key 键
   * @param value 值
   */
  public void put(String key, Object value) {
    this.data.put(key, value);
    this.change = true;
  }

  /**
   * 获取值
   *
   * @param key 键
   * @return value
   */
  public Object get(String key) {
    return this.data.get(key);
  }

  /**
   * 删除值
   *
   * @param key 键
   */
  public void remove(String key) {
    this.data.remove(key);
    this.change = true;
  }

  /**
   * 设置manager
   *
   * @param manager sessionManager
   */
  public void setManager(SessionManager manager) {
    this.manager = manager;
  }
}
