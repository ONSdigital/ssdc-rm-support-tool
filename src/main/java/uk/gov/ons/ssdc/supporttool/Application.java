package uk.gov.ons.ssdc.supporttool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gcp.autoconfigure.pubsub.GcpPubSubAutoConfiguration;
import org.springframework.cloud.gcp.autoconfigure.pubsub.GcpPubSubReactiveAutoConfiguration;

@SpringBootApplication(
    exclude = {GcpPubSubAutoConfiguration.class, GcpPubSubReactiveAutoConfiguration.class})
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
