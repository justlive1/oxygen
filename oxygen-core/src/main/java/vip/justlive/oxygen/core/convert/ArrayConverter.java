package vip.justlive.oxygen.core.convert;


/**
 * 数组类型转换器
 *
 * @author wubo
 */
public interface ArrayConverter {

  /**
   * 是否支持转换
   *
   * @param sourceType 源类型
   * @param targetType 目标类型
   * @return true为支持
   */
  boolean support(Class<?> sourceType, Class<?> targetType);

  /**
   * 转换
   *
   * @param source 源数据
   * @param sourceType 源数据类型
   * @param targetType 目标类型
   * @return 转换后数据
   */
  Object convert(Object source, Class<?> sourceType, Class<?> targetType);

  /**
   * 获取类型对
   *
   * @return 类型对
   */
  default ConverterTypePair pair() {
    return null;
  }
}
