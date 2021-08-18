package uk.gov.ons.ssdc.supporttool.config;

import java.util.TimeZone;
import javax.annotation.PostConstruct;
import org.springframework.cloud.gcp.pubsub.support.converter.JacksonPubSubMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import uk.gov.ons.ssdc.supporttool.utility.ObjectMapperFactory;

@Configuration
@EnableScheduling
public class AppConfig {
  @Bean
  public JacksonPubSubMessageConverter messageConverter() {
    return new JacksonPubSubMessageConverter(ObjectMapperFactory.objectMapper());
  }

  @PostConstruct
  public void init() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
  }
}
