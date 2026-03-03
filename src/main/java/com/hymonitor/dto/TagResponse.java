package com.hymonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Tag response DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagResponse {
    private String id;
    private String name;
    private String color;
    private LocalDateTime createdAt;
}
