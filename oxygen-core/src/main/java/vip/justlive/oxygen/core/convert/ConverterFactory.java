package vip.justlive.oxygen.core.convert;

import java.util.List;

/**
 * 转换工厂，用于将S转换成R的子类
 *
 * @param <S> 泛型类
 * @param <R> 泛型类
 * @author wubo
 */
public interface ConverterFactory<S, R> {

  /**
   * 获取转换器
   *
   * @param targetType 目标类型
   * @param <T> 泛型类
   * @return 转换器
   */
  <T extends R> Converter<S, T> getConverter(Class<T> targetType);

  /**
   * 获取所有支持的转换器
   *
   * @return 转换器列表
   */
  List<Converter<Object, Object>> converters();
}
