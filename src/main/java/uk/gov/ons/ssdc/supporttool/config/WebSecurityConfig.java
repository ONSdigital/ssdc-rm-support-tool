package uk.gov.ons.ssdc.supporttool.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@EnableWebSecurity
@Configuration
public class WebSecurityConfig {

  private static final String TRUSTED_CDN_DOMAIN = "https://cdn.ons.gov.uk/";

  /*
   This is a temp agreed fix/hack to allow the ATs in CI to work with the Support Tool UI.

   The value for this in app.yml is the correct: "upgrade-insecure-requests;"
   This upggrades all requests to https.

   For the ATs in K8s this doesn't work at present, as we use the http route - so for dev manifests we
   set CSP-upgrade-policy = ''.

   Once the ATs work over IAP and go through the 'real' https frontend we can rip this out
  */
  @Value("${CSP-upgrade-policy}")
  private String cspUpgradePolicy;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.headers(
        headers ->
            headers
                .contentSecurityPolicy(
                    cps ->
                        cps.policyDirectives(
                            String.format(
                                "default-src 'self'; manifest-src %s ; style-src 'self' 'unsafe-inline' ; %s block-all-mixed-content",
                                TRUSTED_CDN_DOMAIN, cspUpgradePolicy)))
                .frameOptions(frameOptionsConfig -> frameOptionsConfig.deny())
                .httpStrictTransportSecurity(
                    hstsConfig ->
                        hstsConfig
                            .includeSubDomains(true)
                            .maxAgeInSeconds(31536000)
                            .requestMatcher(AnyRequestMatcher.INSTANCE))
                .referrerPolicy(
                    referrerPolicyConfig ->
                        referrerPolicyConfig.policy(
                            ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .permissionsPolicy(
                    permissionsPolicyConfig ->
                        permissionsPolicyConfig.policy(
                            "accelerometer=(),autoplay=(),camera=(),display-capture=(),document-domain=(),encrypted-media=(),fullscreen=(),geolocation=(),gyroscope=(),magnetometer=(),microphone=(),midi=(),payment=(),picture-in-picture=(),publickey-credentials-get=(),screen-wake-lock=(),sync-xhr=(self),usb=(),xr-spatial-tracking=()")));
    http.csrf(AbstractHttpConfigurer::disable);
    return http.build();
  }
}
