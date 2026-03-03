package com.hymonitor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dashboard response containing summary statistics and website list
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardResponse {
    private long upCount;
    private long downCount;
    private long slowCount;
    private long totalCount;
    private List<WebsiteResponse> websites;
}
