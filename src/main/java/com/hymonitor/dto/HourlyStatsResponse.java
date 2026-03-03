package com.hymonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HourlyStatsResponse {
    private LocalDateTime hourBucket;
    private int checkCount;
    private int upCount;
    private int downCount;
    private int slowCount;
    private Long avgResponseMs;
    private Long minResponseMs;
    private Long maxResponseMs;
}
