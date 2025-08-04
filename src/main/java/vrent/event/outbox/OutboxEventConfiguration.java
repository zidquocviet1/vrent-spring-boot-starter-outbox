package vrent.event.outbox;

import javax.sql.DataSource;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.kafka.core.KafkaTemplate;
import vrent.event.outbox.registry.DelegateOutboxEventRegistry;
import vrent.event.outbox.registry.JdbcOutboxEventRegistry;
import vrent.event.outbox.registry.OutboxEventRegistry;
import vrent.event.outbox.repository.InformationSchemaRepository;
import vrent.event.outbox.repository.OutboxEventRepository;
import vrent.event.outbox.validator.OutboxTableValidator;

@AutoConfiguration
@EnableConfigurationProperties(OutboxEventProperties.class)
@ConditionalOnBean({KafkaAutoConfiguration.class, DataSource.class})
public class OutboxEventConfiguration {

  @Bean
  @ConditionalOnMissingBean(JdbcClient.class)
  public JdbcClient jdbcClient(DataSource dataSource) {
    return JdbcClient.create(dataSource);
  }

  @Bean
  public OutboxTableValidator outboxTableValidator(
      InformationSchemaRepository informationSchemaRepository, OutboxEventProperties properties) {
    return new OutboxTableValidator(informationSchemaRepository, properties.outboxEventTable());
  }

  @Bean
  public InformationSchemaRepository informationSchemaRepository(JdbcClient jdbcClient) {
    return new InformationSchemaRepository(jdbcClient);
  }

  @Bean
  public OutboxEventRepository outboxEventRepository(
      JdbcClient jdbcClient, OutboxEventProperties properties) {
    return new OutboxEventRepository(jdbcClient, properties.outboxEventTable());
  }

  @Bean
  public OutboxEventRegistry outboxEventRegistry(
      OutboxEventRepository outboxEventRepository) {
    return new DelegateOutboxEventRegistry(
        new JdbcOutboxEventRegistry(outboxEventRepository));
  }

  @Bean
  @ConditionalOnBean(KafkaTemplate.class)
  public OutboxEventScheduler outboxEventPublisher(
      OutboxEventRepository outboxEventRepository,
      OutboxEventProperties outboxEventProperties,
      KafkaTemplate<SpecificRecord, SpecificRecord> kafkaTemplate) {
    return new OutboxEventScheduler(outboxEventRepository, outboxEventProperties, kafkaTemplate);
  }

  @Bean
  @ConditionalOnProperty(
      name = "vrent.event.outbox.validateOnStartup",
      havingValue = "true",
      matchIfMissing = false)
  public OutboxTableValidationRunner outboxTableValidationRunner(OutboxTableValidator validator) {
    return new OutboxTableValidationRunner(validator);
  }

  public static class OutboxTableValidationRunner {
    private final OutboxTableValidator validator;

    public OutboxTableValidationRunner(OutboxTableValidator validator) {
      this.validator = validator;
      // Run validation immediately when bean is created
      validateDataSource();
    }

    /**
     * Validates the outbox table structure
     *
     * @throws IllegalStateException if validation fails
     */
    public void validateDataSource() {
      validator.validate();
    }
  }
}
