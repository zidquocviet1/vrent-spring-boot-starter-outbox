package vrent.event.outbox;

import java.io.IOException;
import java.util.List;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import vrent.event.outbox.repository.OutboxEventRepository;
import vrent.event.outbox.repository.entity.OutboxEvent;

public class OutboxEventScheduler {
  private static final Logger logger = LoggerFactory.getLogger(OutboxEventScheduler.class);

  private final OutboxEventRepository outboxEventRepository;
  private final OutboxEventProperties outboxEventProperties;
  private final KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate;

  public OutboxEventScheduler(
      OutboxEventRepository outboxEventRepository,
      OutboxEventProperties outboxEventProperties,
      KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate) {
    this.outboxEventRepository = outboxEventRepository;
    this.outboxEventProperties = outboxEventProperties;
    this.kafkaTemplate = kafkaTemplate;
  }

  @Scheduled(fixedDelayString = "${vrent.event.outbox.publish-duration-ms}")
  public void publish() {
    if (logger.isDebugEnabled()) {
      logger.debug("Start publishing outbox events");
    }
    final List<OutboxEvent> unprocessedEvents = outboxEventRepository.findUnprocessedEvents();

    for (OutboxEvent event : unprocessedEvents) {
      logger.info("Processing outbox event with id: {}", event.id());

      try {
        final SpecificRecord key;
        if (event.key() == null) {
          key = null;
        } else {
          key = getRecord(Class.forName(event.keyClass()), event.key());
        }
        final SpecificRecord value = getRecord(Class.forName(event.valueClass()), event.value());

        kafkaTemplate.send(event.topic(), key, value);
        outboxEventRepository.markAsProcessed(event.id());

        logger.info("Processed outbox event with id: {}", event.id());
      } catch (Exception e) {
        // if the kafka publish message error, nothing will be happened
        // if the processing save-to-database error, then the message state will not be changed,
        // it will cause a process-duplicated message at the next execution time. but it doesn't
        // matter, because the consumer has implemented idempotence read

        final int retryAttempt = event.retryAttempt();
        if (retryAttempt < outboxEventProperties.failureRetryAttempts()) {
          outboxEventRepository.increaseRetryAttempt(event.id());
        } else {
          logger.error(
              "Failed to process outbox event without retry because of retry exceeded: {}",
              event.id(),
              e);
          outboxEventRepository.markAsError(event.id(), e.getMessage());
        }
      }
    }
  }

  private static SpecificRecord getRecord(Class<?> clazz, String payload) throws IOException {
    final Schema schema = getSchema(clazz);
    final DatumReader<SpecificRecord> specificDatumReader = new SpecificDatumReader<>(schema);
    final Decoder jsonDecoder = DecoderFactory.get().jsonDecoder(schema, payload);

    return specificDatumReader.read(null, jsonDecoder);
  }

  private static Schema getSchema(Class<?> clazz) {
    if (!SpecificRecord.class.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException("Class must be a SpecificRecord: " + clazz.getName());
    }
    try {
      return (Schema) clazz.getField("SCHEMA$").get(null);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get schema from class: " + clazz.getName(), e);
    }
  }
}
