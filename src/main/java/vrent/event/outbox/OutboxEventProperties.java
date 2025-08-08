package vrent.event.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** Configuration properties for the outbox event starter. */
@ConfigurationProperties(prefix = "vrent.event.outbox")
public record OutboxEventProperties(
    /** The name of the outbox event table in the database. Default: "outbox_event" */
    @DefaultValue("outbox_event") String outboxEventTable,

    /** The duration between publishing attempts. */
    int publishDurationMs,

    /** The number of retry attempts for failed publishing. */
    int failureRetryAttempts) {}
