package vrent.event.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuration properties for the outbox event starter. */
@ConfigurationProperties(prefix = "vrent.event.outbox")
public record OutboxEventProperties(
    /** The name of the outbox event table in the database. Default: "outbox_event" */
    @DefaultValue("outbox_event") String outboxEventTable,

    /** The serializer type to use for event payloads. Supported values: AVRO, JSON */
    Serializer serializerType,

    /** The location of Avro schema files (only used with AVRO serializer). */
    String avroSchemaLocation,

    /** The duration between publishing attempts. */
    int publishDurationMs,

    /** The number of retry attempts for failed publishing. */
    int failureRetryAttempts,

    /** The URL of the schema registry (only used with AVRO serializer). */
    String schemaRegistryUrl,

    /**
     * Whether to validate the outbox table structure on startup. When set to true, the starter will
     * validate that the outbox table exists with all required columns and indexes. Default: false
     */
    boolean validateOnStartup) {
  /** Creates a new instance with default values. */

  /** Serializer types for event payloads. */
  public enum Serializer {
    /** Apache Avro serialization. */
    AVRO,

    /** JSON serialization. */
    JSON
  }
}
