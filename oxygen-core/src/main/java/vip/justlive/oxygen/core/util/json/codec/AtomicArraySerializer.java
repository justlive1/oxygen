package vip.justlive.oxygen.core.util.json.codec;

import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;
import lombok.RequiredArgsConstructor;
import vip.justlive.oxygen.core.util.ClassUtils;

/**
 * atomic array序列化
 *
 * @author wubo
 */
@RequiredArgsConstructor
public class AtomicArraySerializer implements Serializer {

  private final List<Serializer> serializers;

  @Override
  public boolean supported(Class<?> type, Object value) {
    return type == AtomicIntegerArray.class || type == AtomicLongArray.class
        || type == AtomicReferenceArray.class;
  }

  @Override
  public void serialize(Object value, StringBuilder buf) {
    Object array = ClassUtils.getValue(value, "array");
    Serializer serializer = Serializer.lookup(serializers, array);
    if (serializer != null) {
      serializer.serialize(array, buf);
    }
  }
}
