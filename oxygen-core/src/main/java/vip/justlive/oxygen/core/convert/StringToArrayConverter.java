package vip.justlive.oxygen.core.convert;

import java.lang.reflect.Array;
import vip.justlive.oxygen.core.constant.Constants;

/**
 * String - 数组 转换器
 *
 * @author wubo
 */
public class StringToArrayConverter implements ArrayConverter {

  private final ConverterService converterService;

  public StringToArrayConverter(ConverterService converterService) {
    this.converterService = converterService;
  }

  @Override
  public boolean support(Class<?> sourceType, Class<?> targetType) {
    return String.class.equals(sourceType) && targetType.isArray();
  }

  @Override
  public Object convert(Object source, Class<?> sourceType, Class<?> targetType) {

    if (support(sourceType, targetType)) {

      String[] sourceArray = source.toString().split(Constants.COMMA);
      Class<?> componentType = targetType.getComponentType();
      Object targetArray = Array.newInstance(componentType, sourceArray.length);

      for (int i = 0; i < sourceArray.length; i++) {
        Object value = converterService.convert(sourceArray[i], componentType);
        Array.set(targetArray, i, value);
      }
      return targetArray;
    }

    throw new IllegalArgumentException(String
        .format("unsupported [%s] convert from [%s] to [%s]", source, sourceType, targetType));
  }

  @Override
  public ConverterTypePair pair() {
    return ConverterTypePair.create(String.class, Array.class);
  }

}
