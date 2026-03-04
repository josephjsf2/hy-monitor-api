package com.hymonitor.controller;

import com.hymonitor.dto.CheckResultResponse;
import com.hymonitor.dto.HourlyStatsResponse;
import com.hymonitor.dto.WebsiteRequest;
import com.hymonitor.dto.WebsiteResponse;
import com.hymonitor.dto.WebsiteTagRequest;
import com.hymonitor.service.StatsService;
import com.hymonitor.service.WebsiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/websites")
@RequiredArgsConstructor
public class WebsiteController {

    private final WebsiteService websiteService;
    private final StatsService statsService;

    @GetMapping
    public ResponseEntity<List<WebsiteResponse>> getAllWebsites() {
        return ResponseEntity.ok(websiteService.getAllWebsites());
    }

    @PostMapping
    public ResponseEntity<WebsiteResponse> createWebsite(
            @Valid @RequestBody WebsiteRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(websiteService.createWebsite(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WebsiteResponse> updateWebsite(
            @PathVariable UUID id,
            @Valid @RequestBody WebsiteRequest request) {
        return ResponseEntity.ok(websiteService.updateWebsite(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWebsite(@PathVariable UUID id) {
        websiteService.deleteWebsite(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/tags")
    public ResponseEntity<WebsiteResponse> setWebsiteTags(
            @PathVariable UUID id,
            @RequestBody WebsiteTagRequest request) {
        return ResponseEntity.ok(websiteService.setWebsiteTags(id, request));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<Page<CheckResultResponse>> getWebsiteHistory(
            @PathVariable UUID id,
            @PageableDefault(size = 100) Pageable pageable) {
        return ResponseEntity.ok(websiteService.getWebsiteHistory(id, pageable));
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<List<HourlyStatsResponse>> getWebsiteStats(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(statsService.getHourlyStats(id, from, to));
    }
}
