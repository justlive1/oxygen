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
package vip.justlive.oxygen.web.i18n;

import java.util.Locale;
import javax.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.aop.After;
import vip.justlive.oxygen.core.aop.Before;
import vip.justlive.oxygen.core.aop.Catching;
import vip.justlive.oxygen.core.config.ConfigFactory;
import vip.justlive.oxygen.core.config.CoreConf;
import vip.justlive.oxygen.core.constant.Constants;
import vip.justlive.oxygen.core.i18n.Lang;
import vip.justlive.oxygen.core.ioc.Bean;
import vip.justlive.oxygen.web.http.Request;
import vip.justlive.oxygen.web.mapping.Mapping;

/**
 * i18n 切面
 *
 * @author wubo
 */
@Slf4j
@Bean
public class I18nAop {

  @Before(annotation = Mapping.class)
  public void before() {
    Request request = Request.current();
    if (request == null) {
      return;
    }
    CoreConf conf = ConfigFactory.load(CoreConf.class);
    String localeStr = request.getParam(conf.getI18nParamKey());
    if (localeStr == null || localeStr.length() == 0) {
      HttpSession session = request.getOriginalRequest().getSession(false);
      if (session == null) {
        return;
      }
      Locale locale = (Locale) session.getAttribute(conf.getI18nSessionKey());
      if (locale != null) {
        Lang.setThreadLocale(locale);
      }
    } else {
      String[] arr = localeStr.split(Constants.UNDERSCORE);
      if (arr.length != 2) {
        log.warn("locale [{}] is incorrect", localeStr);
      } else {
        Locale locale = new Locale(arr[0], arr[1]);
        if (log.isDebugEnabled()) {
          log.debug("change locale from [{}] to [{}]", Lang.currentThreadLocale(), locale);
        }
        request.getOriginalRequest().getSession().setAttribute(conf.getI18nSessionKey(), locale);
        Lang.setThreadLocale(locale);
      }
    }
  }

  @After(annotation = Mapping.class)
  public void after() {
    Lang.clearThreadLocale();
  }

  @Catching(annotation = Mapping.class)
  public void catching() {
    Lang.clearThreadLocale();
  }
}
