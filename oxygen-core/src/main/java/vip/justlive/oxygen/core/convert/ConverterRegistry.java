package vip.justlive.oxygen.core.convert;

/**
 * 转换器注册
 *
 * @author wubo
 */
public interface ConverterRegistry {

  /**
   * 注册转换器
   *
   * @param converter 转换器
   * @return 返回当前对象
   */
  ConverterRegistry addConverter(Converter<?, ?> converter);

  /**
   * 注册转换器工厂
   *
   * @param factory 转换器工厂
   * @return 返回当前对象
   */
  ConverterRegistry addConverterFactory(ConverterFactory<?, ?> factory);

  /**
   * 注册数组类型转换器
   *
   * @param converter 转换器
   * @return 返回当前对象
   */
  ConverterRegistry addArrayConverter(ArrayConverter converter);
}
