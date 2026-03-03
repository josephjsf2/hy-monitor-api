package com.hymonitor.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Website check result entity
 */
@Entity
@Table(name = "CHECK_RESULT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "WEBSITE_ID", nullable = false)
    private UUID websiteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private WebsiteStatus status;

    @Column(name = "HTTP_CODE")
    private Integer httpCode;

    @Column(name = "RESPONSE_MS")
    private Long responseMs;

    @Column(name = "ERROR_MSG", length = 1000)
    private String errorMsg;

    @Column(name = "CHECKED_AT", nullable = false)
    private LocalDateTime checkedAt;
}
