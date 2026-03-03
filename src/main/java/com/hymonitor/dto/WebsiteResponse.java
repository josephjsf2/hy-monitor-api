package com.hymonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Website response DTO with latest check status
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebsiteResponse {
    private String id;
    private String url;
    private String alias;
    private boolean enabled;
    private List<TagResponse> tags;

    // Latest check result fields
    private String status;          // UP, DOWN, SLOW, or null if never checked
    private Integer httpCode;
    private Long responseMs;
    private LocalDateTime lastCheckedAt;

    private LocalDateTime createdAt;
}
