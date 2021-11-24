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
package vip.justlive.oxygen.core.util.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import lombok.extern.slf4j.Slf4j;

/**
 * 可多次使用的收集器
 *
 * @author wubo
 */
@Slf4j
public class Collector<S> implements DiagnosticListener<S> {

  private final BlockingQueue<Diagnostic<? extends S>> queue = new LinkedBlockingQueue<>();

  @Override
  public void report(Diagnostic<? extends S> diagnostic) {
    if (diagnostic == null) {
      return;
    }
    if (!queue.offer(diagnostic)) {
      log.warn("diagnostic {} cannot add to queue", diagnostic);
    }
  }

  /**
   * flush
   *
   * @return list
   */
  public List<Diagnostic<? extends S>> flush() {
    List<Diagnostic<? extends S>> diagnostics = new ArrayList<>();
    queue.drainTo(diagnostics);
    return diagnostics;
  }
}
