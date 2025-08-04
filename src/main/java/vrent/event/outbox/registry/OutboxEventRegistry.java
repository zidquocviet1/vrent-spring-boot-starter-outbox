package vrent.event.outbox.registry;

import org.springframework.lang.Nullable;

public interface OutboxEventRegistry {
  <K, V> void save(
      String aggregateId, String aggregateType, String topic, @Nullable K key, V value);
}
