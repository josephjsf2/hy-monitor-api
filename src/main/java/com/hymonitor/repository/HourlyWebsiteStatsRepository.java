package com.hymonitor.repository;

import com.hymonitor.entity.HourlyWebsiteStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HourlyWebsiteStatsRepository extends JpaRepository<HourlyWebsiteStats, UUID> {

    List<HourlyWebsiteStats> findByWebsite_IdAndHourBucketBetweenOrderByHourBucketAsc(
            UUID websiteId, LocalDateTime from, LocalDateTime to);

    Optional<HourlyWebsiteStats> findByWebsite_IdAndHourBucket(UUID websiteId, LocalDateTime hourBucket);

    @Modifying
    @Query("DELETE FROM HourlyWebsiteStats h WHERE h.hourBucket < :cutoff")
    int deleteByHourBucketBefore(@Param("cutoff") LocalDateTime cutoff);
}
