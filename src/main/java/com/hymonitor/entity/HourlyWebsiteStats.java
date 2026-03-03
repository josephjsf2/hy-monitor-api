package com.hymonitor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "HOURLY_WEBSITE_STATS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HourlyWebsiteStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "WEBSITE_ID", nullable = false)
    private UUID websiteId;

    @Column(name = "HOUR_BUCKET", nullable = false)
    private LocalDateTime hourBucket;

    @Column(name = "CHECK_COUNT", nullable = false)
    private int checkCount;

    @Column(name = "UP_COUNT", nullable = false)
    private int upCount;

    @Column(name = "DOWN_COUNT", nullable = false)
    private int downCount;

    @Column(name = "SLOW_COUNT", nullable = false)
    private int slowCount;

    @Column(name = "AVG_RESPONSE_MS")
    private Long avgResponseMs;

    @Column(name = "MIN_RESPONSE_MS")
    private Long minResponseMs;

    @Column(name = "MAX_RESPONSE_MS")
    private Long maxResponseMs;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;
}
