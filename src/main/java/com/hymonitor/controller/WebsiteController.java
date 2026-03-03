package com.hymonitor.controller;

import com.hymonitor.dto.HourlyStatsResponse;
import com.hymonitor.dto.WebsiteRequest;
import com.hymonitor.dto.WebsiteResponse;
import com.hymonitor.dto.WebsiteTagRequest;
import com.hymonitor.entity.CheckResult;
import com.hymonitor.service.StatsService;
import com.hymonitor.service.WebsiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for website management
 */
@RestController
@RequestMapping("/api/websites")
@RequiredArgsConstructor
public class WebsiteController {

    private final WebsiteService websiteService;
    private final StatsService statsService;

    /**
     * Get all websites with their latest check status
     * @return list of websites
     */
    @GetMapping
    public ResponseEntity<List<WebsiteResponse>> getAllWebsites() {
        return ResponseEntity.ok(websiteService.getAllWebsites());
    }

    /**
     * Create a new website
     * @param request website creation request
     * @param userDetails authenticated user
     * @return created website
     */
    @PostMapping
    public ResponseEntity<WebsiteResponse> createWebsite(
            @Valid @RequestBody WebsiteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // For now, pass username as userId (simplified)
        return ResponseEntity.ok(websiteService.createWebsite(request, userDetails.getUsername()));
    }

    /**
     * Update a website
     * @param id website ID
     * @param request update request
     * @return updated website
     */
    @PutMapping("/{id}")
    public ResponseEntity<WebsiteResponse> updateWebsite(
            @PathVariable String id,
            @Valid @RequestBody WebsiteRequest request) {
        return ResponseEntity.ok(websiteService.updateWebsite(id, request));
    }

    /**
     * Delete a website
     * @param id website ID
     * @return no content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebsite(@PathVariable String id) {
        websiteService.deleteWebsite(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Set tags for a website
     * @param id website ID
     * @param request tag assignment request
     * @return updated website
     */
    @PutMapping("/{id}/tags")
    public ResponseEntity<WebsiteResponse> setWebsiteTags(
            @PathVariable String id,
            @RequestBody WebsiteTagRequest request) {
        return ResponseEntity.ok(websiteService.setWebsiteTags(id, request));
    }

    /**
     * Get check history for a website
     * @param id website ID
     * @param page page number (default 0)
     * @param size page size (default 100)
     * @return list of check results
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<CheckResult>> getWebsiteHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(websiteService.getWebsiteHistory(id, page, size));
    }

    /**
     * Get hourly statistics for a website
     * @param id website ID
     * @param from start time (ISO 8601)
     * @param to end time (ISO 8601)
     * @return list of hourly statistics
     */
    @GetMapping("/{id}/stats")
    public ResponseEntity<List<HourlyStatsResponse>> getWebsiteStats(
            @PathVariable String id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(statsService.getHourlyStats(id, from, to));
    }
}
