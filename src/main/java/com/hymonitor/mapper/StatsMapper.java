package com.hymonitor.mapper;

import com.hymonitor.dto.HourlyStatsResponse;
import com.hymonitor.entity.HourlyWebsiteStats;
import org.springframework.stereotype.Component;

@Component
public class StatsMapper {

    public HourlyStatsResponse toResponse(HourlyWebsiteStats stats) {
        return new HourlyStatsResponse(
                stats.getHourBucket(),
                stats.getCheckCount(),
                stats.getUpCount(),
                stats.getDownCount(),
                stats.getSlowCount(),
                stats.getAvgResponseMs(),
                stats.getMinResponseMs(),
                stats.getMaxResponseMs()
        );
    }
}
