package vip.justlive.oxygen.core.convert;


/**
 * String - Character转换器
 *
 * @author wubo
 */
public class StringToCharacterConverter implements Converter<String, Character> {

  @Override
  public Character convert(String source) {
    if (source == null || source.isEmpty()) {
      return null;
    }
    if (source.length() > 1) {
      throw new IllegalArgumentException(
          String.format("Can only convert String[%s] to Character", source));
    }
    return source.charAt(0);
  }

  @Override
  public ConverterTypePair pair() {
    return ConverterTypePair.create(String.class, Character.class);
  }

}
