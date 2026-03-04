package com.hymonitor.repository;

import com.hymonitor.entity.CheckResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public interface CheckResultRepository extends JpaRepository<CheckResult, UUID> {

    Optional<CheckResult> findFirstByWebsite_IdOrderByCheckedAtDesc(UUID websiteId);

    Page<CheckResult> findByWebsite_IdOrderByCheckedAtDesc(UUID websiteId, Pageable pageable);

    @Query(value = "SELECT * FROM (SELECT *, ROW_NUMBER() OVER (PARTITION BY website_id ORDER BY checked_at DESC) as rn " +
                   "FROM check_result WHERE website_id IN :websiteIds) sub WHERE rn = 1", nativeQuery = true)
    List<CheckResult> findLatestByWebsiteIds(@Param("websiteIds") List<UUID> websiteIds);

    void deleteByWebsite_Id(UUID websiteId);

    @Modifying
    @Query("DELETE FROM CheckResult cr WHERE cr.checkedAt < :cutoff")
    int deleteByCheckedAtBefore(@Param("cutoff") LocalDateTime cutoff);

    List<CheckResult> findByWebsite_IdAndCheckedAtBetween(UUID websiteId, LocalDateTime from, LocalDateTime to);
}
