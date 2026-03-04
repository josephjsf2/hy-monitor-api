package com.hymonitor.dto;

import java.time.LocalDateTime;

public record HourlyStatsResponse(
        LocalDateTime hourBucket,
        int checkCount,
        int upCount,
        int downCount,
        int slowCount,
        Long avgResponseMs,
        Long minResponseMs,
        Long maxResponseMs
) {
}
