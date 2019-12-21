/*
 * Copyright (C) 2019 the original author or authors.
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

package vip.justlive.oxygen.core.net.aio;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import vip.justlive.oxygen.core.net.aio.core.AioListener;
import vip.justlive.oxygen.core.net.aio.core.ChannelContext;
import vip.justlive.oxygen.core.net.aio.core.Client;
import vip.justlive.oxygen.core.net.aio.core.GroupContext;
import vip.justlive.oxygen.core.net.aio.core.Server;
import vip.justlive.oxygen.core.net.aio.protocol.LengthFrame;
import vip.justlive.oxygen.core.net.aio.protocol.LengthFrameHandler;
import vip.justlive.oxygen.core.util.ThreadUtils;

/**
 * @author wubo
 */
@Slf4j
public class AioTest {

  //  @Test
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
    List<Client> clients = new ArrayList<>(max);
    for (int i = 0; i < max; i++) {
      group = new GroupContext(new LengthFrameHandler() {
        @Override
        public Object beat(ChannelContext channelContext) {
          return null;
        }
      });
      Client client = new Client(group);
      client.connect(address, new InetSocketAddress(i + 30000));
      clients.add(client);
    }

    Random r = new Random();
    for (int i = 0; i < 100; i++) {
      clients.get(r.nextInt(max)).write(new LengthFrame().setBody(String.valueOf(i).getBytes()));
    }

    ThreadUtils.sleep(1000);

    clients.forEach(Client::close);
    clients.clear();

    ThreadUtils.sleep(2000);

    server.stop();
  }

  @Test
  public void test() throws IOException {
    InetSocketAddress address = new InetSocketAddress(10087);
    GroupContext group = new GroupContext(new LengthFrameHandler() {
      @Override
      public void handle(Object data, ChannelContext channelContext) {
        LengthFrame frame = (LengthFrame) data;
        System.out.println(new String(frame.getBody()));
      }
    });
    Server server = new Server(group);
    server.start(address);

    group = new GroupContext(new LengthFrameHandler());
    Client client = new Client(group);
    client.connect(new InetSocketAddress("localhost", 10087));

    Random r = new Random();
    for (int i = 0; i < 100; i++) {
      client.write(new LengthFrame().setBody(String.valueOf(i).getBytes()));

      int rx = r.nextInt(100);
      if (rx > 50) {
        ThreadUtils.sleep(rx);
      }
    }

    ThreadUtils.sleep(2000);
  }

}