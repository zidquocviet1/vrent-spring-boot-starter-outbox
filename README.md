# Vrent Spring Boot Starter Outbox

A Spring Boot starter for implementing the outbox pattern with messaging.

## Overview

This starter provides functionality to implement the outbox pattern for reliable event publishing. It stores events in a database table before publishing them to a message broker, ensuring that events are not lost even if the message broker is temporarily unavailable.

## Features

- Automatic configuration of outbox event handling
- Support for different serialization formats (JSON, Avro)
- Database table validation
- Configurable retry mechanism for failed publishing attempts

## Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>vrent</groupId>
    <artifactId>vrent-spring-boot-starter-outbox</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration

Configure the starter using the following properties in your `application.properties` or `application.yml`:

```properties
# Required properties
vrent.event.outbox.outboxEventTable=outbox_event
vrent.event.outbox.serializerType=JSON

# Optional properties
vrent.event.outbox.publishDuration=PT1M
vrent.event.outbox.failureRetryAttempts=3
vrent.event.outbox.validateOnStartup=false

# Required for Avro serialization
vrent.event.outbox.avroSchemaLocation=/path/to/avro/schemas
vrent.event.outbox.schemaRegistryUrl=http://localhost:8081
```

## Database Setup

The starter requires a database table with the following structure:

```sql
CREATE TABLE outbox_event (
    id VARCHAR(36) PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL
);

CREATE INDEX outbox_event_status_idx ON outbox_event(status);
```

## Checking the Data Source and Triggering Validation

### Automatic Validation on Startup

To validate the outbox table structure automatically on application startup, set the following property:

```properties
vrent.event.outbox.validateOnStartup=true
```

When this property is enabled, the starter will check that:
1. The outbox table exists
2. All required columns exist (id, topic, payload, created_at, status)
3. The required index exists (status_idx)

If any validation fails, the application will fail to start with an appropriate error message.

### Manual Validation

To manually trigger validation, you can inject the `OutboxTableValidationRunner` bean and call the `validateDataSource()` method:

```java
@Service
public class MyService {
    private final OutboxTableValidationRunner validator;
    
    public MyService(OutboxTableValidationRunner validator) {
        this.validator = validator;
    }
    
    public void checkDataSource() {
        try {
            validator.validateDataSource();
            System.out.println("Validation successful!");
        } catch (IllegalStateException e) {
            System.err.println("Validation failed: " + e.getMessage());
        }
    }
}
```

Alternatively, you can directly inject the `OutboxTableValidator` bean:

```java
@Service
public class MyService {
    private final OutboxTableValidator validator;
    
    public MyService(OutboxTableValidator validator) {
        this.validator = validator;
    }
    
    public void checkDataSource() {
        try {
            validator.validateAll();
            System.out.println("Validation successful!");
        } catch (IllegalStateException e) {
            System.err.println("Validation failed: " + e.getMessage());
        }
    }
    
    // You can also validate specific aspects
    public void checkTable() {
        validator.ensureMandatoryTableExists();
    }
    
    public void checkColumns() {
        validator.ensureMandatoryColumnsExists();
    }
    
    public void checkIndexes() {
        validator.ensureMandatoryIndexesExists();
    }
}
```

## Usage

To use the outbox event functionality, inject the `OutboxEventRegistry` bean and call the `save` method:

```java
@Service
public class OrderService {
    private final OutboxEventRegistry outboxEventRegistry;
    
    public OrderService(OutboxEventRegistry outboxEventRegistry) {
        this.outboxEventRegistry = outboxEventRegistry;
    }
    
    public void createOrder(Order order) {
        // Business logic...
        
        // Save event to outbox
        outboxEventRegistry.save("orders", order);
    }
}
```

## License

[License information]