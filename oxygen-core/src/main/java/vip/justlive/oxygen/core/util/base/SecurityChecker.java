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

package vip.justlive.oxygen.core.util.base;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全检查
 *
 * @author wubo
 */
public class SecurityChecker {

  private final List<Checker> checkers = new ArrayList<>();

  /**
   * 检查权限
   */
  public void checkPermission() {
    checkers.forEach(Checker::check);
  }

  /**
   * 添加检查
   *
   * @param checker 检查
   * @return SecurityChecker
   */
  public SecurityChecker addChecker(Checker checker) {
    checkers.add(checker);
    return this;
  }

  /**
   * 清楚检查
   */
  public void clear() {
    checkers.clear();
  }

  /**
   * 删除检查
   *
   * @param checker 检查
   * @return SecurityChecker
   */
  public SecurityChecker delChecker(Checker checker) {
    checkers.remove(checker);
    return this;
  }
}
