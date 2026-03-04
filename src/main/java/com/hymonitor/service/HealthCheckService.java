package com.hymonitor.service;

import com.hymonitor.entity.CheckResult;
import com.hymonitor.entity.MonitoredWebsite;
import com.hymonitor.entity.WebsiteStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private static final int MAX_ERROR_MSG_LENGTH = 1000;

    private final RestClient monitorRestClient;

    @Value("${app.monitor.slow-threshold-ms}")
    private long slowThresholdMs;

    public CheckResult check(MonitoredWebsite website) {
        long startTime = System.currentTimeMillis();
        try {
            var response = monitorRestClient.get()
                    .uri(website.getUrl())
                    .retrieve()
                    .toBodilessEntity();
            long responseMs = System.currentTimeMillis() - startTime;
            int httpCode = response.getStatusCode().value();

            WebsiteStatus status;
            if (httpCode >= 200 && httpCode < 300) {
                status = responseMs >= slowThresholdMs ? WebsiteStatus.SLOW : WebsiteStatus.UP;
            } else {
                status = WebsiteStatus.DOWN;
            }

            return CheckResult.builder()
                    .website(website)
                    .status(status)
                    .httpCode(httpCode)
                    .responseMs(responseMs)
                    .checkedAt(LocalDateTime.now())
                    .build();
        } catch (Exception e) {
            long responseMs = System.currentTimeMillis() - startTime;
            String errorMsg = e.getMessage() != null
                ? e.getMessage().substring(0, Math.min(e.getMessage().length(), MAX_ERROR_MSG_LENGTH))
                : "Unknown error";

            return CheckResult.builder()
                    .website(website)
                    .status(WebsiteStatus.DOWN)
                    .responseMs(responseMs)
                    .errorMsg(errorMsg)
                    .checkedAt(LocalDateTime.now())
                    .build();
        }
    }
}
