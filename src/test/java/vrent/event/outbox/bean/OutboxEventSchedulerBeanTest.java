package vrent.event.outbox.bean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import vrent.event.outbox.OutboxEventScheduler;

@SpringBootTest
public class OutboxEventSchedulerBeanTest {
  @Configuration
  static class CronJobConfiguration {
    @DynamicPropertySource
    static void outboxPublisherTypeProperties(DynamicPropertyRegistry registry) {
      registry.add("vrent.event.outbox.publisher-type", () -> "cron-job");
    }
  }

  @Configuration
  static class DebeziumConfiguration {
    @DynamicPropertySource
    static void outboxPublisherTypeProperties(DynamicPropertyRegistry registry) {
      registry.add("vrent.event.outbox.publisher-type", () -> "debezium");
    }
  }

  @Test
  void shouldNotRegisterBeanIfNoCronJobIsConfigured() {
    new ApplicationContextRunner()
        .withUserConfiguration(DebeziumConfiguration.class)
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(OutboxEventScheduler.class);
              assertThat(context).doesNotHaveBean("outboxEventScheduler");
            });
  }

  @Test
  void shouldRegisterBeanIfCronJobIsConfigured() {
    new ApplicationContextRunner()
        .withUserConfiguration(CronJobConfiguration.class)
        .run(
            context -> {
              assertThat(context).doesNotHaveBean(OutboxEventScheduler.class);
              assertThat(context).doesNotHaveBean("outboxEventScheduler");
            });
  }
}
