package com.hymonitor.controller;

import com.hymonitor.dto.DashboardResponse;
import com.hymonitor.dto.WebsiteResponse;
import com.hymonitor.service.WebsiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dashboard controller providing summary statistics and website overview
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final WebsiteService websiteService;

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard() {
        List<WebsiteResponse> websites = websiteService.getAllWebsites();
        long upCount = websites.stream().filter(w -> "UP".equals(w.getStatus())).count();
        long downCount = websites.stream().filter(w -> "DOWN".equals(w.getStatus())).count();
        long slowCount = websites.stream().filter(w -> "SLOW".equals(w.getStatus())).count();

        DashboardResponse response = DashboardResponse.builder()
            .upCount(upCount)
            .downCount(downCount)
            .slowCount(slowCount)
            .totalCount(websites.size())
            .websites(websites)
            .build();
        return ResponseEntity.ok(response);
    }
}
