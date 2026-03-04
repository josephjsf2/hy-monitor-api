package com.hymonitor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "HOURLY_WEBSITE_STATS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class HourlyWebsiteStats {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "WEBSITE_ID", nullable = false)
    private MonitoredWebsite website;

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

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
