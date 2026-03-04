package com.hymonitor.dto;

import java.time.LocalDateTime;

public record CheckResultResponse(
        String id,
        String websiteId,
        String status,
        Integer httpCode,
        Long responseMs,
        String errorMsg,
        LocalDateTime checkedAt
) {
}
