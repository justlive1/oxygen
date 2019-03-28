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
package vip.justlive.oxygen.web.router;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import vip.justlive.oxygen.core.domain.Resp;
import vip.justlive.oxygen.web.annotation.Mapping;
import vip.justlive.oxygen.web.annotation.Param;
import vip.justlive.oxygen.web.annotation.Router;

/**
 * 基础请求 router
 *
 * @author wubo
 */
@Router("/common")
public class CommonRouter {

  /**
   * 当前服务器毫秒值
   *
   * @return 当前时间
   */
  @Mapping("/currentTime")
  public Resp currentTime() {
    return Resp.success(System.currentTimeMillis());
  }

  /**
   * 获取服务器日期 无时分秒
   *
   * @param offset 偏移量 与当前日期的偏移
   * @return 服务器日期
   */
  @Mapping("/localDate")
  public Resp localDate(@Param(value = "offset", defaultValue = "0") Integer offset) {
    return Resp.success(
        LocalDate.now().plusDays(offset).atStartOfDay(ZoneOffset.systemDefault()).toInstant()
            .toEpochMilli());
  }

  /**
   * 获取服务器日期时间
   *
   * @param offset 偏移量 与当前日期的偏移
   * @return 服务器日期时间
   */
  @Mapping("/localDateTime")
  public Resp localDateTime(@Param(value = "offset", defaultValue = "0") Integer offset) {
    return Resp.success(
        LocalDateTime.now().plusDays(offset).atZone(ZoneOffset.systemDefault()).toInstant()
            .toEpochMilli());
  }

}
