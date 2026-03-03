package com.hymonitor.repository;

import com.hymonitor.entity.CheckResult;
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

/**
 * Repository for CheckResult entity
 */
@Repository
public interface CheckResultRepository extends JpaRepository<CheckResult, UUID> {

    /**
     * Find the latest check result for a website
     * @param websiteId the website ID
     * @return Optional of the latest CheckResult
     */
    Optional<CheckResult> findFirstByWebsiteIdOrderByCheckedAtDesc(UUID websiteId);

    /**
     * Find check results for a website with pagination, ordered by checked time descending
     * @param websiteId the website ID
     * @param pageable pagination parameters
     * @return List of CheckResults
     */
    List<CheckResult> findByWebsiteIdOrderByCheckedAtDesc(UUID websiteId, Pageable pageable);

    /**
     * Find the latest check result for each website in the given list
     * Uses window function for efficient query performance
     * @param websiteIds list of website IDs
     * @return List of latest CheckResults for each website
     */
    @Query(value = "SELECT * FROM (SELECT *, ROW_NUMBER() OVER (PARTITION BY website_id ORDER BY checked_at DESC) as rn " +
                   "FROM check_result WHERE website_id IN :websiteIds) sub WHERE rn = 1", nativeQuery = true)
    List<CheckResult> findLatestByWebsiteIds(@Param("websiteIds") List<UUID> websiteIds);

    /**
     * Delete all check results for a website
     * @param websiteId the website ID
     */
    void deleteByWebsiteId(UUID websiteId);

    /**
     * Delete all check results older than the cutoff date
     * @param cutoff the cutoff date
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM CheckResult cr WHERE cr.checkedAt < :cutoff")
    int deleteByCheckedAtBefore(@Param("cutoff") LocalDateTime cutoff);

    /**
     * Find check results for a website within a time range
     * @param websiteId the website ID
     * @param from start time (inclusive)
     * @param to end time (exclusive)
     * @return List of CheckResults
     */
    List<CheckResult> findByWebsiteIdAndCheckedAtBetween(UUID websiteId, LocalDateTime from, LocalDateTime to);
}
