package com.hymonitor.scheduler;

import com.hymonitor.repository.CheckResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled task for data retention management
 * Purges old check results to maintain database size
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataRetentionScheduler {

    private final CheckResultRepository checkResultRepository;

    @Value("${app.monitor.retention-days:180}")
    private int retentionDays;

    /**
     * Run daily at 2 AM to purge old check results
     * Deletes check results older than the configured retention period
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeOldCheckResults() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deleted = checkResultRepository.deleteByCheckedAtBefore(cutoff);
        LOGGER.info("Data retention: deleted {} check results older than {} days", deleted, retentionDays);
    }
}
