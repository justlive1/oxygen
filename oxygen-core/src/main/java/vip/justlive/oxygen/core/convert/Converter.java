package vip.justlive.oxygen.core.convert;


/**
 * 类型转换器
 *
 * @param <S> 泛型类
 * @param <T> 泛型类
 * @author wubo
 */
public interface Converter<S, T> {


  /**
   * 类型转换
   *
   * @param source 源
   * @return 转换类
   */
  T convert(S source);

  /**
   * 获取类型对
   *
   * @return 类型对
   */
  default ConverterTypePair pair() {
    return null;
  }
}
