package com.hymonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket message for real-time status updates
 * Published to clients when a health check completes
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusUpdateMessage {

    private String websiteId;
    private String url;
    private String alias;
    private String status;
    private Integer httpCode;
    private Long responseMs;
    private LocalDateTime checkedAt;
}
