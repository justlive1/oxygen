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

package vip.justlive.oxygen.core.util.net.aio;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 组合监听
 *
 * @author wubo
 */
@Data
public class ComposeAioListener implements AioListener {

  private final List<AioListener> listeners = new ArrayList<>();

  public ComposeAioListener add(AioListener listener) {
    if (listener == null || listeners.contains(listener)) {
      return this;
    }
    if (listener instanceof ComposeAioListener) {
      ((ComposeAioListener) listener).listeners.forEach(this::add);
    } else {
      listeners.add(listener);
    }
    return this;
  }

  @Override
  public void onConnected(ChannelContext channelContext) {
    listeners.forEach(listener -> listener.onConnected(channelContext));
  }

  @Override
  public void onClosed(ChannelContext channelContext) {
    listeners.forEach(listener -> listener.onClosed(channelContext));
  }

  @Override
  public void onWriteHandled(ChannelContext channelContext, Object data, Throwable throwable) {
    listeners.forEach(listener -> listener.onWriteHandled(channelContext, data, throwable));
  }

  @Override
  public void onReadHandled(ChannelContext channelContext, Object data, Throwable throwable) {
    listeners.forEach(listener -> listener.onReadHandled(channelContext, data, throwable));
  }
}
