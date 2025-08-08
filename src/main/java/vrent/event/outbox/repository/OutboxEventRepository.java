package vrent.event.outbox.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import vrent.event.outbox.repository.entity.OutboxEvent;
import vrent.event.outbox.repository.mapper.OutboxEventMapper;

public class OutboxEventRepository {
  private static final String INSERT_SQL =
      "INSERT INTO %s (id, aggregate_id, aggregate_type, topic, key_class, key, value_class, value, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
  private static final String GET_UNPROCESSED_SQL =
      "SELECT * FROM %s WHERE processed = FALSE AND is_error = FALSE ORDER BY created_at";
  private static final String MARK_AS_PROCESSED_SQL =
      "UPDATE %s SET processed = TRUE, processed_at = NOW() WHERE id = ?";
  private static final String MARK_AS_ERROR_SQL =
      "UPDATE %s SET is_error = TRUE, error_message = ? WHERE id = ?";
  private static final String INCREASE_RETRY_ATTEMPT_SQL =
      "UPDATE %s SET retry_attempt = retry_attempt + 1 WHERE id = ?";

  private static final Logger logger = LoggerFactory.getLogger(OutboxEventRepository.class);

  private final JdbcClient jdbcClient;

  private final String tableName;

  public OutboxEventRepository(JdbcClient jdbcClient, String tableName) {
    this.jdbcClient = jdbcClient;
    this.tableName = tableName;
  }

  @Transactional
  public void save(
      String aggregateId, String aggregateType, String topic, String valueClass, String valueJson) {
    save(aggregateId, aggregateType, topic, null, null, valueClass, valueJson);
  }

  @Transactional
  public void save(
      String aggregateId,
      String aggregateType,
      String topic,
      String keyClass,
      String keyJson,
      String valueClass,
      String valueJson) {
    if (logger.isDebugEnabled()) {
      logger.debug(
          "Inserting event into outbox table with aggregateId: {}, aggregateType: {}, topic: {}, keyClass: {}, valueClass: {}",
          aggregateId,
          aggregateType,
          topic,
          keyClass,
          valueClass);
    }
    jdbcClient
        .sql(String.format(INSERT_SQL, tableName))
        .param(UUID.randomUUID())
        .param(aggregateId)
        .param(aggregateType)
        .param(topic)
        .param(keyClass)
        .param(keyJson)
        .param(valueClass)
        .param(valueJson)
        .param(LocalDateTime.now())
        .update();
  }

  public List<OutboxEvent> findUnprocessedEvents() {
    return jdbcClient
        .sql(String.format(GET_UNPROCESSED_SQL, tableName))
        .query(new OutboxEventMapper())
        .list();
  }

  @Transactional
  public void markAsProcessed(UUID id) {
    if (logger.isDebugEnabled()) {
      logger.debug("Mark event with id: {} as processed", id);
    }
    jdbcClient.sql(String.format(MARK_AS_PROCESSED_SQL, tableName)).param(id).update();
  }

  @Transactional
  public void markAsError(UUID id, String errorMessage) {
    if (logger.isDebugEnabled()) {
      logger.debug("Mark event with id: {} as error", id);
    }
    jdbcClient
        .sql(String.format(MARK_AS_ERROR_SQL, tableName))
        .param(errorMessage)
        .param(id)
        .update();
  }

  @Transactional
  public void increaseRetryAttempt(UUID id) {
    if (logger.isDebugEnabled()) {
      logger.debug("Increase retry attempt of event with id: {}", id);
    }
    jdbcClient.sql(String.format(INCREASE_RETRY_ATTEMPT_SQL, tableName)).param(id).update();
  }
}
