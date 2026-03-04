package com.hymonitor.dto;

import java.time.LocalDateTime;

public record TagResponse(String id, String name, String color, LocalDateTime createdAt) {
}
