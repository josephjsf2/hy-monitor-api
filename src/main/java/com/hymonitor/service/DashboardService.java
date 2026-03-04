package com.hymonitor.service;

import com.hymonitor.dto.DashboardResponse;
import com.hymonitor.dto.WebsiteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WebsiteService websiteService;

    @Cacheable("dashboard")
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        List<WebsiteResponse> websites = websiteService.getAllWebsites();
        long upCount = websites.stream().filter(w -> "UP".equals(w.getStatus())).count();
        long downCount = websites.stream().filter(w -> "DOWN".equals(w.getStatus())).count();
        long slowCount = websites.stream().filter(w -> "SLOW".equals(w.getStatus())).count();

        return DashboardResponse.builder()
                .upCount(upCount)
                .downCount(downCount)
                .slowCount(slowCount)
                .totalCount(websites.size())
                .websites(websites)
                .build();
    }
}
