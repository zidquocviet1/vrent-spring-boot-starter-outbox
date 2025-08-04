package vrent.event.outbox.registry;

import org.apache.avro.generic.GenericContainer;
import vrent.event.outbox.repository.OutboxEventRepository;

public class JdbcOutboxEventRegistry implements OutboxEventRegistry {
  private final OutboxEventRepository outboxEventRepository;

  public JdbcOutboxEventRegistry(
      OutboxEventRepository outboxEventRepository) {
    this.outboxEventRepository = outboxEventRepository;
  }

  @Override
  public <K, V> void save(String aggregateId, String aggregateType, String topic, K key, V value) {
    try {
      final String keyClass;
      final String keyJson;
      if (key != null) {
        keyClass = ((GenericContainer) key).getSchema().getFullName();
        keyJson = key.toString();
      } else {
        keyClass = null;
        keyJson = null;
      }
      final String valueClass = ((GenericContainer) value).getSchema().getFullName();

      outboxEventRepository.save(
          aggregateId, aggregateType, topic, keyClass, keyJson, valueClass, value.toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
