package vrent.event.outbox.registry;

import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

public class DelegateOutboxEventRegistry implements OutboxEventRegistry {
  private static final Logger logger = LoggerFactory.getLogger(DelegateOutboxEventRegistry.class);

  private final OutboxEventRegistry delegate;

  public DelegateOutboxEventRegistry(OutboxEventRegistry delegate) {
    this.delegate = delegate;
  }

  @Override
  public <K, V> void save(
      String aggregateId, String aggregateType, String topic, @Nullable K key, V value) {
    if (aggregateId == null) {
      throw new IllegalArgumentException("aggregateId must not be null");
    }
    if (aggregateType == null) {
      throw new IllegalArgumentException("aggregateType must not be null");
    }
    if (topic == null) {
      throw new IllegalArgumentException("topic must not be null");
    }
    if (value == null) {
      throw new IllegalArgumentException("value must not be null");
    }
    boolean isValidAvro = true;
    if (key != null) {
      isValidAvro &= key instanceof SpecificRecord || key instanceof GenericRecord;
    }
    isValidAvro &= value instanceof SpecificRecord || value instanceof GenericRecord;

    if (!isValidAvro) {
      throw new IllegalArgumentException("key and value must be avro record");
    }
    delegate.save(aggregateId, aggregateType, topic, key, value);
  }
}
