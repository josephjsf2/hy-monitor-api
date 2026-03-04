package com.hymonitor.mapper;

import com.hymonitor.dto.CheckResultResponse;
import com.hymonitor.dto.TagResponse;
import com.hymonitor.dto.WebsiteResponse;
import com.hymonitor.entity.CheckResult;
import com.hymonitor.entity.MonitoredWebsite;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class WebsiteMapper {

    public WebsiteResponse toResponse(MonitoredWebsite website, CheckResult latestResult) {
        WebsiteResponse.WebsiteResponseBuilder builder = WebsiteResponse.builder()
                .id(website.getId().toString())
                .url(website.getUrl())
                .alias(website.getAlias())
                .enabled(website.getEnabled())
                .tags(website.getTags().stream()
                        .map(t -> new TagResponse(
                                t.getId().toString(),
                                t.getName(),
                                t.getColor(),
                                t.getCreatedAt()))
                        .collect(Collectors.toList()))
                .createdAt(website.getCreatedAt());

        if (latestResult != null) {
            builder.status(latestResult.getStatus().name())
                    .httpCode(latestResult.getHttpCode())
                    .responseMs(latestResult.getResponseMs())
                    .lastCheckedAt(latestResult.getCheckedAt());
        }
        return builder.build();
    }

    public CheckResultResponse toCheckResultResponse(CheckResult result) {
        return new CheckResultResponse(
                result.getId().toString(),
                result.getWebsite().getId().toString(),
                result.getStatus().name(),
                result.getHttpCode(),
                result.getResponseMs(),
                result.getErrorMsg(),
                result.getCheckedAt()
        );
    }
}
