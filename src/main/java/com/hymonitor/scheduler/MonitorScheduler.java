package com.hymonitor.scheduler;

import com.hymonitor.dto.StatusUpdateMessage;
import com.hymonitor.entity.CheckResult;
import com.hymonitor.entity.MonitoredWebsite;
import com.hymonitor.repository.CheckResultRepository;
import com.hymonitor.repository.MonitoredWebsiteRepository;
import com.hymonitor.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled task for monitoring website health
 * Runs periodically to check all enabled websites and push status updates via WebSocket
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MonitorScheduler {

    private final MonitoredWebsiteRepository websiteRepository;
    private final CheckResultRepository checkResultRepository;
    private final HealthCheckService healthCheckService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Check all enabled websites on a fixed schedule
     * Saves check results and broadcasts status updates via WebSocket
     */
    @Scheduled(fixedRateString = "${app.monitor.interval-ms}")
    public void checkAllWebsites() {
        List<MonitoredWebsite> websites = websiteRepository.findAllByEnabledTrue();
        LOGGER.info("Starting health check for {} websites", websites.size());

        for (MonitoredWebsite website : websites) {
            try {
                CheckResult result = healthCheckService.check(website);
                checkResultRepository.save(result);

                // Push via WebSocket
                StatusUpdateMessage message = StatusUpdateMessage.builder()
                        .websiteId(website.getId().toString())
                        .url(website.getUrl())
                        .alias(website.getAlias())
                        .status(result.getStatus().name())
                        .httpCode(result.getHttpCode())
                        .responseMs(result.getResponseMs())
                        .checkedAt(result.getCheckedAt())
                        .build();
                messagingTemplate.convertAndSend("/topic/status", message);

                LOGGER.debug("Checked {}: {} ({}ms)", website.getUrl(), result.getStatus(), result.getResponseMs());
            } catch (Exception e) {
                LOGGER.error("Error checking website {}: {}", website.getUrl(), e.getMessage());
            }
        }
    }
}
