package com.hymonitor.service;

import com.hymonitor.entity.CheckResult;
import com.hymonitor.entity.MonitoredWebsite;
import com.hymonitor.entity.WebsiteStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

/**
 * Service for performing health checks on monitored websites
 * Handles HTTP requests and determines website status based on response
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private final RestTemplate monitorRestTemplate;

    @Value("${app.monitor.slow-threshold-ms}")
    private long slowThresholdMs;

    /**
     * Perform health check on a monitored website
     * @param website the website to check
     * @return CheckResult containing status, response time, and other metrics
     */
    public CheckResult check(MonitoredWebsite website) {
        long startTime = System.currentTimeMillis();
        try {
            ResponseEntity<String> response = monitorRestTemplate.getForEntity(website.getUrl(), String.class);
            long responseMs = System.currentTimeMillis() - startTime;
            int httpCode = response.getStatusCode().value();

            WebsiteStatus status;
            if (httpCode >= 200 && httpCode < 300) {
                status = responseMs >= slowThresholdMs ? WebsiteStatus.SLOW : WebsiteStatus.UP;
            } else {
                status = WebsiteStatus.DOWN;
            }

            return CheckResult.builder()
                    .websiteId(website.getId().toString())
                    .status(status)
                    .httpCode(httpCode)
                    .responseMs(responseMs)
                    .checkedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            long responseMs = System.currentTimeMillis() - startTime;
            String errorMsg = e.getMessage() != null
                ? e.getMessage().substring(0, Math.min(e.getMessage().length(), 500))
                : "Unknown error";

            return CheckResult.builder()
                    .websiteId(website.getId().toString())
                    .status(WebsiteStatus.DOWN)
                    .responseMs(responseMs)
                    .errorMsg(errorMsg)
                    .checkedAt(LocalDateTime.now())
                    .build();
        }
    }
}
