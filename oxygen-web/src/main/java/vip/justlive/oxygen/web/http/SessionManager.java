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

import lombok.Data;

/**
 * session manager
 *
 * @author wubo
 */
@Data
public class SessionManager {

  private long expired = 3600;
  private SessionStore store = new SessionStoreImpl();

  /**
   * 获取session
   *
   * @param id sessionId
   * @return session
   */
  public Session getSession(String id) {
    return store.get(id);
  }

  /**
   * 创建session
   *
   * @param session session
   */
  public void createSession(Session session) {
    session.setManager(this);
    session.setExpired(expired);
    store.put(session.getId(), session, session.getExpired());
  }

  /**
   * restore
   *
   * @param session session
   */
  public void restoreSession(Session session) {
    if (session != null && session.isChange()) {
      store.put(session.getId(), session, session.getExpired());
    }
  }

  /**
   * 清除session
   */
  public void clear() {
    store.clear();
  }

  /**
   * 删除指定session
   *
   * @param id sessionId
   */
  public void removeSession(String id) {
    store.remove(id);
  }
}
