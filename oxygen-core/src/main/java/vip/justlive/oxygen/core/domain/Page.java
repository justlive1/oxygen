package vip.justlive.oxygen.core.domain;

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
public class Page<T> {

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
