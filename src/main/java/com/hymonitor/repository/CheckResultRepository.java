package com.hymonitor.repository;

import com.hymonitor.entity.CheckResult;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    Optional<CheckResult> findFirstByWebsiteIdOrderByCheckedAtDesc(String websiteId);

    /**
     * Find check results for a website with pagination, ordered by checked time descending
     * @param websiteId the website ID
     * @param pageable pagination parameters
     * @return List of CheckResults
     */
    List<CheckResult> findByWebsiteIdOrderByCheckedAtDesc(String websiteId, Pageable pageable);

    /**
     * Find the latest check result for each website in the given list
     * @param websiteIds list of website IDs
     * @return List of latest CheckResults for each website
     */
    @Query("SELECT cr FROM CheckResult cr WHERE cr.websiteId IN :websiteIds AND cr.checkedAt = " +
           "(SELECT MAX(cr2.checkedAt) FROM CheckResult cr2 WHERE cr2.websiteId = cr.websiteId)")
    List<CheckResult> findLatestByWebsiteIds(@Param("websiteIds") List<String> websiteIds);

    /**
     * Delete all check results for a website
     * @param websiteId the website ID
     */
    void deleteByWebsiteId(String websiteId);
}
