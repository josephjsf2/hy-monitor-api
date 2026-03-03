package com.hymonitor.scheduler;

import com.hymonitor.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatsAggregationScheduler {

    private final StatsService statsService;
    private final CacheManager cacheManager;

    /**
     * Runs at 5 minutes past every hour to aggregate the previous hour's check results
     * Cron: 0 5 * * * * = at 5 minutes past every hour
     */
    @Scheduled(cron = "0 5 * * * *")
    public void aggregateHourlyStats() {
        LOGGER.info("Starting scheduled hourly stats aggregation");

        try {
            // Calculate the start of the previous hour
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime previousHour = now.minusHours(1).truncatedTo(ChronoUnit.HOURS);

            LOGGER.info("Aggregating stats for hour: {}", previousHour);
            statsService.aggregateHour(previousHour);

            // Evict hourly-stats cache to ensure fresh data
            var cache = cacheManager.getCache("hourly-stats");
            if (cache != null) {
                cache.clear();
                LOGGER.info("Evicted hourly-stats cache");
            }

            LOGGER.info("Successfully completed hourly stats aggregation for hour: {}", previousHour);
        } catch (Exception e) {
            LOGGER.error("Failed to aggregate hourly stats: {}", e.getMessage(), e);
        }
    }
}
