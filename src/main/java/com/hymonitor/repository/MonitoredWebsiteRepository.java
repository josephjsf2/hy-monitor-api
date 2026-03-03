package com.hymonitor.repository;

import com.hymonitor.entity.MonitoredWebsite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for MonitoredWebsite entity
 */
@Repository
public interface MonitoredWebsiteRepository extends JpaRepository<MonitoredWebsite, UUID> {

    /**
     * Find all enabled websites
     * @return List of enabled websites
     */
    List<MonitoredWebsite> findAllByEnabledTrue();
}
