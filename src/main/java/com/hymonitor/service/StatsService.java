package com.hymonitor.service;

import com.hymonitor.dto.HourlyStatsResponse;
import com.hymonitor.entity.CheckResult;
import com.hymonitor.entity.HourlyWebsiteStats;
import com.hymonitor.entity.MonitoredWebsite;
import com.hymonitor.entity.WebsiteStatus;
import com.hymonitor.repository.CheckResultRepository;
import com.hymonitor.repository.HourlyWebsiteStatsRepository;
import com.hymonitor.repository.MonitoredWebsiteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final HourlyWebsiteStatsRepository hourlyStatsRepository;
    private final CheckResultRepository checkResultRepository;
    private final MonitoredWebsiteRepository websiteRepository;

    /**
     * Get hourly statistics for a website within a time range
     * Results are cached for performance
     *
     * @param websiteId the website ID
     * @param from start time
     * @param to end time
     * @return list of hourly stats
     */
    @Cacheable(value = "hourly-stats", key = "#websiteId + '_' + #from + '_' + #to")
    public List<HourlyStatsResponse> getHourlyStats(String websiteId, LocalDateTime from, LocalDateTime to) {
        UUID uuid = UUID.fromString(websiteId);
        List<HourlyWebsiteStats> stats = hourlyStatsRepository
                .findByWebsiteIdAndHourBucketBetweenOrderByHourBucketAsc(uuid, from, to);
        return stats.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Aggregate check results for all websites for a specific hour
     * This is called by the scheduler after each hour completes
     *
     * @param hourStart the start of the hour to aggregate
     */
    @Transactional
    public void aggregateHour(LocalDateTime hourStart) {
        LocalDateTime hourEnd = hourStart.plusHours(1);
        LOGGER.info("Starting aggregation for hour: {} to {}", hourStart, hourEnd);

        List<MonitoredWebsite> websites = websiteRepository.findAll();
        LOGGER.info("Aggregating stats for {} websites", websites.size());

        int aggregatedCount = 0;
        for (MonitoredWebsite website : websites) {
            try {
                aggregateForWebsite(website.getId(), hourStart, hourEnd);
                aggregatedCount++;
            } catch (Exception e) {
                LOGGER.error("Failed to aggregate stats for website {}: {}",
                        website.getId(), e.getMessage(), e);
            }
        }

        LOGGER.info("Completed aggregation for hour: {}. Aggregated {} out of {} websites",
                hourStart, aggregatedCount, websites.size());
    }

    /**
     * Aggregate check results for a single website for a specific hour
     * Computes statistics and saves to hourly_website_stats table
     *
     * @param websiteId the website ID
     * @param hourStart start of the hour
     * @param hourEnd end of the hour
     */
    private void aggregateForWebsite(UUID websiteId, LocalDateTime hourStart, LocalDateTime hourEnd) {
        List<CheckResult> results = checkResultRepository
                .findByWebsiteIdAndCheckedAtBetween(websiteId, hourStart, hourEnd);

        if (results.isEmpty()) {
            LOGGER.debug("No check results found for website {} in hour {}", websiteId, hourStart);
            return;
        }

        // Calculate statistics
        int checkCount = results.size();
        int upCount = (int) results.stream().filter(r -> r.getStatus() == WebsiteStatus.UP).count();
        int downCount = (int) results.stream().filter(r -> r.getStatus() == WebsiteStatus.DOWN).count();
        int slowCount = (int) results.stream().filter(r -> r.getStatus() == WebsiteStatus.SLOW).count();

        // Calculate response time statistics (only for UP and SLOW status)
        List<Long> responseTimes = results.stream()
                .filter(r -> r.getResponseMs() != null)
                .map(CheckResult::getResponseMs)
                .collect(Collectors.toList());

        Long avgResponseMs = null;
        Long minResponseMs = null;
        Long maxResponseMs = null;

        if (!responseTimes.isEmpty()) {
            avgResponseMs = (long) responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0.0);
            minResponseMs = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .min()
                    .orElse(0L);
            maxResponseMs = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .max()
                    .orElse(0L);
        }

        // Check if stats already exist for this hour (for idempotency)
        List<HourlyWebsiteStats> existing = hourlyStatsRepository
                .findByWebsiteIdAndHourBucketBetweenOrderByHourBucketAsc(
                        websiteId, hourStart, hourStart.plusMinutes(1));

        HourlyWebsiteStats stats;
        if (existing.isEmpty()) {
            // Create new stats
            stats = HourlyWebsiteStats.builder()
                    .websiteId(websiteId)
                    .hourBucket(hourStart)
                    .checkCount(checkCount)
                    .upCount(upCount)
                    .downCount(downCount)
                    .slowCount(slowCount)
                    .avgResponseMs(avgResponseMs)
                    .minResponseMs(minResponseMs)
                    .maxResponseMs(maxResponseMs)
                    .createdAt(LocalDateTime.now())
                    .build();
        } else {
            // Update existing stats (in case aggregation runs multiple times)
            stats = existing.get(0);
            stats.setCheckCount(checkCount);
            stats.setUpCount(upCount);
            stats.setDownCount(downCount);
            stats.setSlowCount(slowCount);
            stats.setAvgResponseMs(avgResponseMs);
            stats.setMinResponseMs(minResponseMs);
            stats.setMaxResponseMs(maxResponseMs);
        }

        hourlyStatsRepository.save(stats);
        LOGGER.debug("Aggregated stats for website {} in hour {}: {} checks, {} up, {} down, {} slow",
                websiteId, hourStart, checkCount, upCount, downCount, slowCount);
    }

    /**
     * Convert entity to DTO
     *
     * @param stats the entity
     * @return the DTO
     */
    private HourlyStatsResponse toResponse(HourlyWebsiteStats stats) {
        return HourlyStatsResponse.builder()
                .hourBucket(stats.getHourBucket())
                .checkCount(stats.getCheckCount())
                .upCount(stats.getUpCount())
                .downCount(stats.getDownCount())
                .slowCount(stats.getSlowCount())
                .avgResponseMs(stats.getAvgResponseMs())
                .minResponseMs(stats.getMinResponseMs())
                .maxResponseMs(stats.getMaxResponseMs())
                .build();
    }
}
