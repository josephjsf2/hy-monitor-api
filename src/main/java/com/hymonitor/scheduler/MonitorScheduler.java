package com.hymonitor.scheduler;

import com.hymonitor.dto.StatusUpdateMessage;
import com.hymonitor.entity.CheckResult;
import com.hymonitor.entity.MonitoredWebsite;
import com.hymonitor.repository.CheckResultRepository;
import com.hymonitor.repository.MonitoredWebsiteRepository;
import com.hymonitor.service.HealthCheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonitorScheduler {

    private final MonitoredWebsiteRepository websiteRepository;
    private final CheckResultRepository checkResultRepository;
    private final HealthCheckService healthCheckService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CacheManager cacheManager;

    @Scheduled(fixedRateString = "${app.monitor.interval-ms}")
    public void checkAllWebsites() {
        List<MonitoredWebsite> websites = websiteRepository.findAllByEnabledTrue();
        LOGGER.info("Starting health check for {} websites", websites.size());

        for (MonitoredWebsite website : websites) {
            try {
                CheckResult result = healthCheckService.check(website);
                checkResultRepository.save(result);

                StatusUpdateMessage message = new StatusUpdateMessage(
                        website.getId().toString(),
                        website.getUrl(),
                        website.getAlias(),
                        result.getStatus().name(),
                        result.getHttpCode(),
                        result.getResponseMs(),
                        result.getCheckedAt()
                );
                messagingTemplate.convertAndSend("/topic/status", message);

                LOGGER.debug("Checked {}: {} ({}ms)", website.getUrl(), result.getStatus(), result.getResponseMs());
            } catch (Exception e) {
                LOGGER.error("Error checking website {}: {}", website.getUrl(), e.getMessage());
            }
        }

        evictCache("websites");
        evictCache("dashboard");
    }

    private void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}
