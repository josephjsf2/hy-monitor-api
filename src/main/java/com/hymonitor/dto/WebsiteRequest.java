package com.hymonitor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Website create/update request DTO
 */
@Data
public class WebsiteRequest {

    @NotBlank(message = "URL is required")
    private String url;

    private String alias;

    private Boolean enabled = true;
}
