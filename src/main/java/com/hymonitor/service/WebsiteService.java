package com.hymonitor.service;

import com.hymonitor.dto.CheckResultResponse;
import com.hymonitor.dto.WebsiteRequest;
import com.hymonitor.dto.WebsiteResponse;
import com.hymonitor.dto.WebsiteTagRequest;
import com.hymonitor.entity.AppUser;
import com.hymonitor.entity.CheckResult;
import com.hymonitor.entity.MonitoredWebsite;
import com.hymonitor.entity.Tag;
import com.hymonitor.exception.ResourceNotFoundException;
import com.hymonitor.mapper.WebsiteMapper;
import com.hymonitor.repository.AppUserRepository;
import com.hymonitor.repository.CheckResultRepository;
import com.hymonitor.repository.MonitoredWebsiteRepository;
import com.hymonitor.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WebsiteService {

    private final MonitoredWebsiteRepository websiteRepository;
    private final TagRepository tagRepository;
    private final CheckResultRepository checkResultRepository;
    private final AppUserRepository userRepository;
    private final WebsiteMapper websiteMapper;

    @Cacheable(value = "websites")
    @Transactional(readOnly = true)
    public List<WebsiteResponse> getAllWebsites() {
        List<MonitoredWebsite> websites = websiteRepository.findAll();
        List<UUID> websiteIds = websites.stream()
                .map(MonitoredWebsite::getId)
                .collect(Collectors.toList());

        Map<UUID, CheckResult> latestResults = new HashMap<>();
        if (!websiteIds.isEmpty()) {
            checkResultRepository.findLatestByWebsiteIds(websiteIds)
                    .forEach(cr -> latestResults.put(cr.getWebsite().getId(), cr));
        }

        return websites.stream()
                .map(w -> websiteMapper.toResponse(w, latestResults.get(w.getId())))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"websites", "dashboard"}, allEntries = true)
    public WebsiteResponse createWebsite(WebsiteRequest request, String username) {
        AppUser owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        MonitoredWebsite website = MonitoredWebsite.builder()
                .url(request.getUrl())
                .alias(request.getAlias())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .createdBy(username)
                .owner(owner)
                .build();
        websiteRepository.save(website);
        return websiteMapper.toResponse(website, null);
    }

    @CacheEvict(value = {"websites", "dashboard"}, allEntries = true)
    public WebsiteResponse updateWebsite(UUID id, WebsiteRequest request) {
        MonitoredWebsite website = websiteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Website not found"));

        website.setUrl(request.getUrl());
        website.setAlias(request.getAlias());
        if (request.getEnabled() != null) {
            website.setEnabled(request.getEnabled());
        }
        websiteRepository.save(website);

        CheckResult latest = checkResultRepository
                .findFirstByWebsite_IdOrderByCheckedAtDesc(id)
                .orElse(null);
        return websiteMapper.toResponse(website, latest);
    }

    @CacheEvict(value = {"websites", "dashboard"}, allEntries = true)
    public void deleteWebsite(UUID id) {
        checkResultRepository.deleteByWebsite_Id(id);
        websiteRepository.deleteById(id);
    }

    @CacheEvict(value = {"websites", "dashboard"}, allEntries = true)
    public WebsiteResponse setWebsiteTags(UUID websiteId, WebsiteTagRequest request) {
        MonitoredWebsite website = websiteRepository.findById(websiteId)
                .orElseThrow(() -> new ResourceNotFoundException("Website not found"));

        Set<Tag> tags = new HashSet<>();
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            List<UUID> tagUuids = request.tagIds().stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
            tags = new HashSet<>(tagRepository.findAllById(tagUuids));
        }
        website.setTags(tags);
        websiteRepository.save(website);

        CheckResult latest = checkResultRepository
                .findFirstByWebsite_IdOrderByCheckedAtDesc(websiteId)
                .orElse(null);
        return websiteMapper.toResponse(website, latest);
    }

    @Transactional(readOnly = true)
    public Page<CheckResultResponse> getWebsiteHistory(UUID websiteId, Pageable pageable) {
        return checkResultRepository.findByWebsite_IdOrderByCheckedAtDesc(websiteId, pageable)
                .map(websiteMapper::toCheckResultResponse);
    }
}
