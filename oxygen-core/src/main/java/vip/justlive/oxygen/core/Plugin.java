/*
 * Copyright (C) 2018 justlive1
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
package vip.justlive.oxygen.core;

/**
 * 插件
 *
 * @author wubo
 */
public interface Plugin extends Comparable<Plugin> {

  /**
   * 插件优先级
   * <br>
   * 越小优先级越高
   * <br>
   * 约定系统插件为负数，自定义插件为正数
   *
   * @return order
   */
  default int order() {
    return Integer.MAX_VALUE;
  }

  /**
   * 启动
   */
  default void start() {
  }

  /**
   * 停止
   */
  default void stop() {
  }

  /**
   * 使用order进行比较
   *
   * @param o compareTo
   * @return result
   */
  @Override
  default int compareTo(Plugin o) {
    return Integer.compare(order(), o.order());
  }
}
