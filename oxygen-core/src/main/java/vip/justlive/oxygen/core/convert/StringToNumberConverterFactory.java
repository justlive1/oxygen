package vip.justlive.oxygen.core.convert;

import java.util.ArrayList;
import java.util.List;
import vip.justlive.oxygen.core.util.NumberUtils;

/**
 * String - Number 解析器
 *
 * @author wubo
 * @see Byte
 * @see Short
 * @see Integer
 * @see Long
 * @see java.math.BigInteger
 * @see Float
 * @see Double
 * @see java.math.BigDecimal
 */
public class StringToNumberConverterFactory implements ConverterFactory<String, Number> {

  @Override
  public <T extends Number> Converter<String, T> getConverter(Class<T> targetType) {
    return new StringToNumber<>(targetType);
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<Converter<Object, Object>> converters() {
    List<Converter<Object, Object>> converters = new ArrayList<>();
    for (Class<? extends Number> clazz : NumberUtils.STANDARD_NUMBER_TYPES) {
      Converter<?, ?> c = new StringToNumber<>(clazz);
      converters.add((Converter<Object, Object>) c);
    }
    return converters;
  }

  private static final class StringToNumber<T extends Number> implements Converter<String, T> {

    private final Class<T> targetType;

    public StringToNumber(Class<T> targetType) {
      this.targetType = targetType;
    }

    @Override
    public T convert(String source) {
      if (source == null || source.isEmpty()) {
        return null;
      }
      return NumberUtils.parseNumber(source, this.targetType);
    }

    @Override
    public ConverterTypePair pair() {
      return ConverterTypePair.create(String.class, targetType);
    }
  }
}
