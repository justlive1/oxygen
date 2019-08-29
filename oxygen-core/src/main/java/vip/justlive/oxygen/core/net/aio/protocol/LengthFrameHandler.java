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

package vip.justlive.oxygen.core.net.aio.protocol;

import java.nio.ByteBuffer;
import vip.justlive.oxygen.core.exception.Exceptions;
import vip.justlive.oxygen.core.net.aio.core.AioHandler;
import vip.justlive.oxygen.core.net.aio.core.ChannelContext;

/**
 * 消息头指定消息长度和类型处理
 *
 * @author wubo
 */
public class LengthFrameHandler implements AioHandler {

  private static final LengthFrame BEAT = new LengthFrame().setType(-1);

  @Override
  public ByteBuffer encode(Object data, ChannelContext channelContext) {
    if (data instanceof LengthFrame) {
      LengthFrame frame = (LengthFrame) data;
      int bodySize = 0;
      if (frame.getBody() != null) {
        bodySize = frame.getBody().length;
      }

      ByteBuffer buffer = ByteBuffer.allocate(LengthFrame.BASE_LENGTH + bodySize);
      buffer.putInt(bodySize);
      buffer.putInt(frame.getType());
      if (bodySize > 0) {
        buffer.put(frame.getBody());
      }
      return buffer;
    }
    throw Exceptions.fail(String.format("参数类型不匹配, %s -> %s", data.getClass(), LengthFrame.class));
  }

  @Override
  public Object decode(ByteBuffer buffer, int readableSize, ChannelContext channelContext) {
    if (readableSize < LengthFrame.BASE_LENGTH) {
      return null;
    }
    int bodySize = buffer.getInt();
    if (readableSize - LengthFrame.BASE_LENGTH < bodySize) {
      return null;
    }
    LengthFrame frame = new LengthFrame().setType(buffer.getInt());
    byte[] bytes = new byte[bodySize];
    buffer.get(bytes);
    return frame.setBody(bytes);
  }

  @Override
  public void handle(Object data, ChannelContext channelContext) {
    // nothing
  }

  @Override
  public Object beat(ChannelContext channelContext) {
    return BEAT;
  }
}
