package vrent.event.outbox.repository.entity;

import java.time.LocalDate;
import java.util.UUID;

public record OutboxEvent(
    UUID id,
    String aggregateId,
    String aggregateType,
    String topic,
    String keyClass,
    String key,
    String valueClass,
    String value,
    boolean processed,
    LocalDate createdAt,
    LocalDate updatedAt,
    LocalDate processedAt,
    boolean isError,
    String errorMessage,
    int retryAttempt,
    LocalDate lastRetryAt) {}
