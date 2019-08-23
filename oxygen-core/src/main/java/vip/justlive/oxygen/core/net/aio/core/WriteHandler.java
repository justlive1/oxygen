/*
 * Copyright (C) 2019 justlive1
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License
 *  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing permissions and limitations under
 *  the License.
 */

package vip.justlive.oxygen.core.net.aio.core;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.Semaphore;
import lombok.Data;

/**
 * aio写操作处理
 *
 * @author wubo
 */
@Data
public class WriteHandler implements CompletionHandler<Integer, Semaphore> {

  private final ChannelContext channelContext;

  @Override
  public void completed(Integer result, Semaphore semaphore) {
    channelContext.setLastSentAt(System.currentTimeMillis());
    semaphore.release();
  }

  @Override
  public void failed(Throwable exc, Semaphore semaphore) {
    semaphore.release();
  }
}
