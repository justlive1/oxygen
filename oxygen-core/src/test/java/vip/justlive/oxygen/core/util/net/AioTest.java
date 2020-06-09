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

package vip.justlive.oxygen.core.util.net;


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import vip.justlive.oxygen.core.util.base.SystemUtils;
import vip.justlive.oxygen.core.util.concurrent.ThreadUtils;
import vip.justlive.oxygen.core.util.net.aio.AioListener;
import vip.justlive.oxygen.core.util.net.aio.ChannelContext;
import vip.justlive.oxygen.core.util.net.aio.Client;
import vip.justlive.oxygen.core.util.net.aio.GroupContext;
import vip.justlive.oxygen.core.util.net.aio.LengthFrame;
import vip.justlive.oxygen.core.util.net.aio.LengthFrameHandler;
import vip.justlive.oxygen.core.util.net.aio.Server;

/**
 * @author wubo
 */
@Slf4j
public class AioTest {

  //    @Test
  public void c1000k() throws Exception {

    InetSocketAddress address = new InetSocketAddress("localhost", 10086);
    GroupContext group = new GroupContext(new LengthFrameHandler());
    group.setAioListener(new AioListener() {
      @Override
      public void onConnected(ChannelContext channelContext) {
        log.info("aio connected {}  and current size {}", channelContext,
            channelContext.getGroupContext().getChannels().size());
      }
    });
    Server server = new Server(group);
    server.start(address);

    int max = 10000;
    List<ChannelContext> clients = new ArrayList<>(max);
    group = new GroupContext(new LengthFrameHandler() {
      @Override
      public Object beat(ChannelContext channelContext) {
        return null;
      }
    });
    Client client = new Client(group);
    for (int i = 0; i < max; i++) {
      clients.add(client.connect(address, new InetSocketAddress(i + 30000)));
    }

    Random r = new Random();
    for (int i = 0; i < 100; i++) {
      clients.get(r.nextInt(max))
          .write(new LengthFrame().setBody(String.valueOf(i).getBytes()));
    }

    ThreadUtils.sleep(1000);

    clients.clear();
    client.close();

    ThreadUtils.sleep(2000);

    server.stop();
  }

  @Test
  public void test() throws IOException {

    LoggerContext ctx = (LoggerContext) LoggerFactory.getILoggerFactory();
    ctx.getLogger("root").setLevel(Level.INFO);

    log.info("no use future...");
    System.setProperty("aio.write.future", "false");
    run();

    log.info("use future...");
    System.setProperty("aio.write.future", "true");
    run();
  }

  //  @Test
  public void test1() throws IOException {
    int port = SystemUtils.findAvailablePort();
    InetSocketAddress address = new InetSocketAddress("localhost", port);

    server(address);

    GroupContext group = new GroupContext(new LengthFrameHandler());
    group.setRetryEnabled(true).setRetryInterval(1000L).setRetryMaxAttempts(10)
        .setBeatInterval(1000);
    Client client = new Client(group);
    client.connect(address);

    ThreadUtils.sleep(5000);

    server(address);

    ThreadUtils.sleep(5000);

  }

  //  @Test
  public void test2() throws IOException {
    int port = SystemUtils.findAvailablePort();
    InetSocketAddress address = new InetSocketAddress("localhost", port);
    GroupContext group = new GroupContext(new LengthFrameHandler());
    Server server = new Server(group);
    server.start(address);

    group = new GroupContext(new LengthFrameHandler());
    group.setBeatInterval(3000);
    Client client = new Client(group);
    ChannelContext channel = client.connect(address);

    ThreadUtils.sleep(4500);
    channel.write(new LengthFrame().setBody("asdfg".getBytes()));

    ThreadUtils.sleep(8000);
  }

  private void server(InetSocketAddress address) throws IOException {
    GroupContext group = new GroupContext(new LengthFrameHandler());
    Server server = new Server(group);
    server.start(address);

    new Thread(() -> {
      ThreadUtils.sleep(2000);
      server.stop();
    }).start();

  }

  //  @Test
  public void tx() throws IOException {
    int port = SystemUtils.findAvailablePort();
    new Thread(() -> {
      InetSocketAddress address = new InetSocketAddress(port);
      GroupContext group1 = new GroupContext(new LengthFrameHandler());
      Server server = new Server(group1);
      try {
        server.start(address);
      } catch (IOException e) {
        e.printStackTrace();
      }

      ThreadUtils.sleep(4000);
      server.stop();

    }).start();

    ThreadUtils.sleep(2000);

    List<Client> clients = new ArrayList<>();
    clients.add(ad(port));
    clients.add(ad(port));
    clients.add(ad(port));

    ThreadUtils.sleep(4000);

    clients.forEach(client -> System.out.println(client.getGroupContext().getChannels()));
  }

  private Client ad(int port) throws IOException {
    GroupContext group = new GroupContext(new LengthFrameHandler());
    Client client = new Client(group);

    for (int i = 0; i < 2; i++) {
      ChannelContext channel = client.connect(new InetSocketAddress("localhost", port));
      channel.join();
    }

    return client;
  }

  private void run() throws IOException {
    int port = SystemUtils.findAvailablePort();
    ThreadUtils.sleep(500);
    InetSocketAddress address = new InetSocketAddress(port);
    AtomicLong current = new AtomicLong();
    int max = 10000;
    GroupContext group = new GroupContext(new LengthFrameHandler() {
      @Override
      public void handle(Object data, ChannelContext channelContext) {
        LengthFrame frame = (LengthFrame) data;
        int p = Integer.parseInt(new String(frame.getBody()));
        if (p == max - 1) {
          current.set(System.currentTimeMillis());
        }
        channelContext.write(frame);
      }
    });
    Server server = new Server(group);
    server.start(address);

    group = new GroupContext(new LengthFrameHandler());
    Client client = new Client(group);
    client.connect(new InetSocketAddress("localhost", port)).join();

    for (int i = 0; i < 5; i++) {
      a(max, client, current);
    }

    client.close();
    server.stop();
  }

  private void a(int max, Client client, AtomicLong current) {
    long now = System.currentTimeMillis();

    for (int i = 0; i < max; i++) {
      client.getGroupContext().getChannels().values().iterator().next()
          .write(new LengthFrame().setBody(String.valueOf(i).getBytes()));
    }

    ThreadUtils.sleep(3000);

    System.out.printf("last %s ,duration %s \n", current, current.get() - now);
  }
}