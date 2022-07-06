package uk.gov.ons.ssdc.supporttool.config;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.headers()
        .defaultsDisabled()
        .frameOptions()
        .deny()
        .contentTypeOptions()
        .and()
        .httpStrictTransportSecurity()
        .requestMatcher(AnyRequestMatcher.INSTANCE)
        .includeSubDomains(true)
        .maxAgeInSeconds(31536000)
        .and()
        .referrerPolicy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER)
        .and()
        .contentSecurityPolicy(
            "default-src 'self'; style-src 'self' 'unsafe-inline' ; upgrade-insecure-requests; block-all-mixed-content");

    http.csrf().disable();
  }
}
