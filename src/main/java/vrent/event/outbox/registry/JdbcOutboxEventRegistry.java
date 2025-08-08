package vrent.event.outbox.registry;

import org.apache.avro.generic.GenericContainer;
import vrent.event.outbox.repository.OutboxEventRepository;

public class JdbcOutboxEventRegistry implements OutboxEventRegistry {
  private final OutboxEventRepository outboxEventRepository;

  public JdbcOutboxEventRegistry(OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Override
  public <K, V> void save(String aggregateId, String aggregateType, String topic, K key, V value) {
    try {
      final String valueClass = ((GenericContainer) value).getSchema().getFullName();
      if (key == null) {
        outboxEventRepository.save(aggregateId, aggregateType, topic, valueClass, value.toString());
      } else {
        outboxEventRepository.save(
            aggregateId,
            aggregateType,
            topic,
            ((GenericContainer) key).getSchema().getFullName(),
            key.toString(),
            valueClass,
            value.toString());
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
