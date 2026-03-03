package com.hymonitor.dto;

import lombok.Data;

import java.util.List;

/**
 * Request DTO for setting website tags
 */
@Data
public class WebsiteTagRequest {
    private List<String> tagIds;
}
