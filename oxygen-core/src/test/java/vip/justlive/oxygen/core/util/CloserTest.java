package vip.justlive.oxygen.core.util;

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
