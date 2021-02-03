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
package vip.justlive.oxygen.jdbc;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import vip.justlive.oxygen.core.aop.Aspect;
import vip.justlive.oxygen.core.aop.Aspect.TYPE;
import vip.justlive.oxygen.core.aop.Invocation;
import vip.justlive.oxygen.core.bean.Bean;
import vip.justlive.oxygen.core.util.base.Strings;

/**
 * 数据源切换切面，需要配合oxygen-core使用
 *
 * @author wubo
 */
@Bean
@Slf4j
public class DataSourceAop {

  private static final ThreadLocal<Deque<String>> HOLDER = ThreadLocal.withInitial(ArrayDeque::new);

  @Aspect(annotation = DataSource.class, type = TYPE.BEFORE)
  public void before(Invocation ivt) {
    DataSource ds = ivt.getMethod().getAnnotation(DataSource.class);
    if (ds == null) {
      return;
    }

    String current = Strings.firstNonNull(HOLDER.get().peek(), Jdbc.PRIMARY_KEY);
    HOLDER.get().push(ds.value());
    if (Objects.equals(ds.value(), current)) {
      return;
    }
    if (log.isDebugEnabled()) {
      log.debug("[DS] E [{}]->[{}], M-[{}] T-[{}]", current, ds.value(),
          methodToString(ivt.getMethod()), ivt.getTarget());
    }
    Jdbc.use(ds.value());
  }

  @Aspect(annotation = DataSource.class, type = TYPE.AFTER)
  public void after(Invocation ivt) {
    DataSource ds = ivt.getMethod().getAnnotation(DataSource.class);
    if (ds == null) {
      return;
    }

    Deque<String> deque = HOLDER.get();
    String dataSource = deque.poll();
    String pre = Strings.firstNonNull(deque.peek(), Jdbc.PRIMARY_KEY);

    if (deque.isEmpty()) {
      HOLDER.remove();
    }

    if (Objects.equals(dataSource, pre)) {
      return;
    }

    Jdbc.use(pre);
    if (log.isDebugEnabled()) {
      log.debug("[DS] L [{}]->[{}], M-[{}] T-[{}]", dataSource, pre,
          methodToString(ivt.getMethod()), ivt.getTarget());
    }
  }

  private String methodToString(Method method) {
    return method.getDeclaringClass().getName() + Strings.OCTOTHORP + method.getName();
  }
}
