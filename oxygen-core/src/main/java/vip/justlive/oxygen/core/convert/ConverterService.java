package vip.justlive.oxygen.core.convert;


/**
 * 类型转换服务
 *
 * @author wubo
 */
public interface ConverterService {

  /**
   * 类型是否支持转换
   *
   * @param source 源
   * @param target 目标类型
   * @return true是支持
   */
  boolean canConverter(Class<?> source, Class<?> target);

  /**
   * 转换到指定类型
   *
   * @param source 源
   * @param targetType 目标类型
   * @param <T> 泛型类
   * @return 转换值
   */
  <T> T convert(Object source, Class<T> targetType);
}
