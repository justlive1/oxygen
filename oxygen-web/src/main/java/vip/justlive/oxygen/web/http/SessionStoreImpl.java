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

import vip.justlive.oxygen.core.util.ExpiringMap;

/**
 * 本地session实现
 *
 * @author wubo
 */
public class SessionStoreImpl implements SessionStore {

  private static final ExpiringMap<String, Session> SESSIONS = ExpiringMap.create();

  @Override
  public Session get(String id) {
    return SESSIONS.get(id);
  }

  @Override
  public void put(String id, Session session, long expired) {
    SESSIONS.put(id, session, expired);
  }

  @Override
  public void clear() {
    SESSIONS.clear();
  }

  @Override
  public void remove(String id) {
    SESSIONS.remove(id);
  }
}
