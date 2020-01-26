package vip.justlive.oxygen.core.util.json.codec;

import java.lang.ref.Reference;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;

/**
 * Reference 序列化
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class ReferenceSerializer implements Serializer {

  private final List<Serializer> serializers;

  @Override
  public boolean supported(Class<?> type, Object value) {
    return Reference.class.isAssignableFrom(type) || type == AtomicReference.class;
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    Object obj;
    if (value instanceof AtomicReference) {
      obj = ((AtomicReference) value).get();
    } else {
      obj = ((Reference<?>) value).get();
    }
    Serializer serializer = Serializer.lookup(serializers, obj);
    if (serializer != null) {
      serializer.serialize(obj, buf);
    }
  }
}
