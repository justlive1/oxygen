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
package vip.justlive.oxygen.core.util.base;

import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页
 *
 * @param <T> 泛型
 * @author wubo
 */
@Data
@NoArgsConstructor
public class Page<T> implements Serializable {

  private static final long serialVersionUID = 1L;
  /**
   * 第几页
   */
  private Integer pageIndex;

  /**
   * 每页条数
   */
  private Integer pageSize;

  /**
   * 总计
   */
  private Long totalNumber;

  /**
   * 数据集合
   */
  @SuppressWarnings("squid:S1948")
  private List<T> items;

  /**
   * 总页数
   */
  private Integer totalPage;

  /**
   * 前一页
   */
  private Integer preIndex;

  /**
   * 下一页
   */
  private Integer nextIndex;

  public Page(Integer pageIndex, Integer pageSize, Long totalNumber, List<T> items) {
    this.pageIndex = pageIndex;
    this.pageSize = pageSize;
    this.totalNumber = totalNumber;
    this.items = items;
    compute();
  }

  private void compute() {
    if (pageSize != null && pageSize > 0) {
      long page = totalNumber / pageSize;
      if (totalNumber % pageSize != 0) {
        page++;
      }
      this.totalPage = (int) page;
      this.preIndex = Math.max(1, pageIndex - 1);
      this.nextIndex = Math.min(pageIndex + 1, totalPage);
    }
  }

}
