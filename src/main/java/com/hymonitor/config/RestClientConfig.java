package com.hymonitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate configuration for health check monitoring
 * Configures connection and read timeouts for HTTP health checks
 */
@Configuration
public class RestClientConfig {

    @Value("${app.monitor.timeout-ms}")
    private int timeoutMs;

    /**
     * Configure RestTemplate bean with proper timeouts for monitoring
     * @return configured RestTemplate for health checks
     */
    @Bean
    public RestTemplate monitorRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));
        return new RestTemplate(factory);
    }
}
