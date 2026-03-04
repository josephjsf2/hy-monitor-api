package com.hymonitor.service;

import com.hymonitor.dto.HourlyStatsResponse;
import com.hymonitor.entity.CheckResult;
import com.hymonitor.entity.HourlyWebsiteStats;
import com.hymonitor.entity.MonitoredWebsite;
import com.hymonitor.entity.WebsiteStatus;
import com.hymonitor.mapper.StatsMapper;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatsService {

    private final HourlyWebsiteStatsRepository hourlyStatsRepository;
    private final CheckResultRepository checkResultRepository;
    private final MonitoredWebsiteRepository websiteRepository;
    private final StatsMapper statsMapper;

    @Cacheable(value = "hourly-stats", key = "#websiteId + '_' + #from + '_' + #to")
    public List<HourlyStatsResponse> getHourlyStats(UUID websiteId, LocalDateTime from, LocalDateTime to) {
        List<HourlyWebsiteStats> stats = hourlyStatsRepository
                .findByWebsite_IdAndHourBucketBetweenOrderByHourBucketAsc(websiteId, from, to);
        return stats.stream()
                .map(statsMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void aggregateHour(LocalDateTime hourStart) {
        LocalDateTime hourEnd = hourStart.plusHours(1);
        LOGGER.info("Starting aggregation for hour: {} to {}", hourStart, hourEnd);

        List<MonitoredWebsite> websites = websiteRepository.findAll();
        LOGGER.info("Aggregating stats for {} websites", websites.size());

        int aggregatedCount = 0;
        for (MonitoredWebsite website : websites) {
            try {
                aggregateForWebsite(website, hourStart, hourEnd);
                aggregatedCount++;
            } catch (Exception e) {
                LOGGER.error("Failed to aggregate stats for website {}: {}",
                        website.getId(), e.getMessage(), e);
            }
        }

        LOGGER.info("Completed aggregation for hour: {}. Aggregated {} out of {} websites",
                hourStart, aggregatedCount, websites.size());
    }

    private void aggregateForWebsite(MonitoredWebsite website, LocalDateTime hourStart, LocalDateTime hourEnd) {
        List<CheckResult> results = checkResultRepository
                .findByWebsite_IdAndCheckedAtBetween(website.getId(), hourStart, hourEnd);

        if (results.isEmpty()) {
            LOGGER.debug("No check results found for website {} in hour {}", website.getId(), hourStart);
            return;
        }

        int checkCount = results.size();
        int upCount = (int) results.stream().filter(r -> r.getStatus() == WebsiteStatus.UP).count();
        int downCount = (int) results.stream().filter(r -> r.getStatus() == WebsiteStatus.DOWN).count();
        int slowCount = (int) results.stream().filter(r -> r.getStatus() == WebsiteStatus.SLOW).count();

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

        Optional<HourlyWebsiteStats> existing = hourlyStatsRepository
                .findByWebsite_IdAndHourBucket(website.getId(), hourStart);

        HourlyWebsiteStats stats;
        if (existing.isEmpty()) {
            stats = HourlyWebsiteStats.builder()
                    .website(website)
                    .hourBucket(hourStart)
                    .checkCount(checkCount)
                    .upCount(upCount)
                    .downCount(downCount)
                    .slowCount(slowCount)
                    .avgResponseMs(avgResponseMs)
                    .minResponseMs(minResponseMs)
                    .maxResponseMs(maxResponseMs)
                    .build();
        } else {
            stats = existing.get();
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
                website.getId(), hourStart, checkCount, upCount, downCount, slowCount);
    }
}
