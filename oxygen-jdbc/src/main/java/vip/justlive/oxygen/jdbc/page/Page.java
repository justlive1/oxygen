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

package vip.justlive.oxygen.jdbc.page;

import java.io.Serializable;
import java.util.List;
import lombok.Data;

/**
 * page row
 *
 * @author wubo
 */
@Data
public class Page<T> implements Serializable {

  private static final long serialVersionUID = -3277571898871978041L;

  private final int pageIndex;
  private final int pageSize;

  private boolean searchCount = true;
  private Long totalNumber;
  private List<T> items;

  private boolean hasPaged;

  int getOffset() {
    return (pageIndex - 1) * pageSize;
  }
}
