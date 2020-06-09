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

package vip.justlive.oxygen.core.util.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author wubo
 */
public class CloserTest {

  @Test
  public void test() {
    AtomicInteger count = new AtomicInteger();
    @SuppressWarnings("resource")
    Closer closer = new Closer();
    try {
      closer.register(new CloseAction(count, 2)).check();
      closer.register(new CloseAction(count, 5)).check();
    } catch (IllegalArgumentException e) {
      closer.thrown(e);
    } finally {
      closer.closeQuietly();
    }
    Assert.assertEquals(7, count.get());
  }

  @RequiredArgsConstructor
  static class CloseAction implements Closeable {

    private final AtomicInteger count;
    private final int delta;

    void check() {
      if (this.delta > 2) {
        throw new IllegalArgumentException("xx");
      }
    }

    @Override
    public void close() throws IOException {
      count.addAndGet(delta);
      throw new IOException("ssx" + delta);
    }
  }
}
