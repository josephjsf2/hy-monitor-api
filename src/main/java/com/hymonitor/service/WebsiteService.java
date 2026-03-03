package com.hymonitor.service;

import com.hymonitor.dto.TagResponse;
import com.hymonitor.dto.WebsiteRequest;
import com.hymonitor.dto.WebsiteResponse;
import com.hymonitor.dto.WebsiteTagRequest;
import com.hymonitor.entity.CheckResult;
import com.hymonitor.entity.MonitoredWebsite;
import com.hymonitor.entity.Tag;
import com.hymonitor.exception.ResourceNotFoundException;
import com.hymonitor.repository.CheckResultRepository;
import com.hymonitor.repository.MonitoredWebsiteRepository;
import com.hymonitor.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing monitored websites
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WebsiteService {

    private final MonitoredWebsiteRepository websiteRepository;
    private final TagRepository tagRepository;
    private final CheckResultRepository checkResultRepository;

    /**
     * Get all websites with their latest check results
     * @return List of website responses
     */
    @Transactional(readOnly = true)
    public List<WebsiteResponse> getAllWebsites() {
        List<MonitoredWebsite> websites = websiteRepository.findAll();
        List<String> websiteIds = websites.stream()
                .map(w -> w.getId().toString())
                .collect(Collectors.toList());

        // Batch fetch latest check results
        Map<String, CheckResult> latestResults = new HashMap<>();
        if (!websiteIds.isEmpty()) {
            checkResultRepository.findLatestByWebsiteIds(websiteIds)
                    .forEach(cr -> latestResults.put(cr.getWebsiteId(), cr));
        }

        return websites.stream()
                .map(w -> toResponse(w, latestResults.get(w.getId().toString())))
                .collect(Collectors.toList());
    }

    /**
     * Create a new monitored website
     * @param request website creation request
     * @param userId the user creating the website
     * @return created website response
     */
    public WebsiteResponse createWebsite(WebsiteRequest request, String userId) {
        MonitoredWebsite website = MonitoredWebsite.builder()
                .url(request.getUrl())
                .alias(request.getAlias())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .createdBy(userId)
                .build();
        websiteRepository.save(website);
        return toResponse(website, null);
    }

    /**
     * Update an existing website
     * @param id website ID
     * @param request update request
     * @return updated website response
     */
    public WebsiteResponse updateWebsite(String id, WebsiteRequest request) {
        UUID websiteId = UUID.fromString(id);
        MonitoredWebsite website = websiteRepository.findById(websiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Website not found"));

        website.setUrl(request.getUrl());
        website.setAlias(request.getAlias());
        if (request.getEnabled() != null) {
            website.setEnabled(request.getEnabled());
        }
        websiteRepository.save(website);

        CheckResult latest = checkResultRepository
                .findFirstByWebsiteIdOrderByCheckedAtDesc(id)
                .orElse(null);
        return toResponse(website, latest);
    }

    /**
     * Delete a website and all its check results
     * @param id website ID
     */
    public void deleteWebsite(String id) {
        checkResultRepository.deleteByWebsiteId(id);
        websiteRepository.deleteById(UUID.fromString(id));
    }

    /**
     * Set tags for a website
     * @param websiteId website ID
     * @param request tag assignment request
     * @return updated website response
     */
    public WebsiteResponse setWebsiteTags(String websiteId, WebsiteTagRequest request) {
        UUID id = UUID.fromString(websiteId);
        MonitoredWebsite website = websiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Website not found"));

        Set<Tag> tags = new HashSet<>();
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<UUID> tagUuids = request.getTagIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            tags = new HashSet<>(tagRepository.findAllById(tagUuids));
        }
        website.setTags(tags);
        websiteRepository.save(website);

        CheckResult latest = checkResultRepository
                .findFirstByWebsiteIdOrderByCheckedAtDesc(websiteId)
                .orElse(null);
        return toResponse(website, latest);
    }

    /**
     * Get check history for a website
     * @param websiteId website ID
     * @param page page number
     * @param size page size
     * @return list of check results
     */
    @Transactional(readOnly = true)
    public List<CheckResult> getWebsiteHistory(String websiteId, int page, int size) {
        return checkResultRepository.findByWebsiteIdOrderByCheckedAtDesc(
                websiteId, PageRequest.of(page, size));
    }

    /**
     * Convert entity to response DTO
     * @param website the website entity
     * @param latestResult the latest check result (nullable)
     * @return website response DTO
     */
    private WebsiteResponse toResponse(MonitoredWebsite website, CheckResult latestResult) {
        WebsiteResponse.WebsiteResponseBuilder builder = WebsiteResponse.builder()
                .id(website.getId().toString())
                .url(website.getUrl())
                .alias(website.getAlias())
                .enabled(website.getEnabled())
                .tags(website.getTags().stream()
                        .map(t -> TagResponse.builder()
                                .id(t.getId().toString())
                                .name(t.getName())
                                .color(t.getColor())
                                .createdAt(t.getCreatedAt())
                                .build())
                        .collect(Collectors.toList()))
                .createdAt(website.getCreatedAt());

        if (latestResult != null) {
            builder.status(latestResult.getStatus().name())
                    .httpCode(latestResult.getHttpCode())
                    .responseMs(latestResult.getResponseMs())
                    .lastCheckedAt(latestResult.getCheckedAt());
        }
        return builder.build();
    }
}
