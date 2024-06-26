package uk.gov.ons.ssdc.supporttool.config;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import com.google.cloud.spring.pubsub.support.PublisherFactory;
import com.google.cloud.spring.pubsub.support.SubscriberFactory;
import com.google.cloud.spring.pubsub.support.converter.JacksonPubSubMessageConverter;
import jakarta.annotation.PostConstruct;
import java.util.TimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import uk.gov.ons.ssdc.supporttool.utility.ObjectMapperFactory;

@Configuration
@EnableScheduling
public class AppConfig {
  @Value("${spring.task.scheduling.pool.size}")
  private int schedulingPoolSize;

  @Value("${logging.profile}")
  private String loggingProfile;

  @Bean
  public PubSubTemplate pubSubTemplate(
      PublisherFactory publisherFactory,
      SubscriberFactory subscriberFactory,
      JacksonPubSubMessageConverter jacksonPubSubMessageConverter) {
    PubSubTemplate pubSubTemplate = new PubSubTemplate(publisherFactory, subscriberFactory);
    pubSubTemplate.setMessageConverter(jacksonPubSubMessageConverter);
    return pubSubTemplate;
  }

  @Bean
  public JacksonPubSubMessageConverter messageConverter() {
    return new JacksonPubSubMessageConverter(ObjectMapperFactory.objectMapper());
  }

  @Bean
  public TaskScheduler taskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setPoolSize(schedulingPoolSize);
    return taskScheduler;
  }

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
}
