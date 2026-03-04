package com.hymonitor.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "MONITORED_WEBSITE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MonitoredWebsite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ID")
    private UUID id;

    @Column(name = "URL", nullable = false, length = 512)
    private String url;

    @Column(name = "ALIAS", length = 100)
    private String alias;

    @Column(name = "ENABLED", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Deprecated
    @Column(name = "CREATED_BY")
    private String createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID")
    private AppUser owner;

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

    @ManyToMany
    @JoinTable(
        name = "WEBSITE_TAG",
        joinColumns = @JoinColumn(name = "WEBSITE_ID"),
        inverseJoinColumns = @JoinColumn(name = "TAG_ID")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "website")
    @Builder.Default
    private List<CheckResult> checkResults = new ArrayList<>();

    @OneToMany(mappedBy = "website")
    @Builder.Default
    private List<HourlyWebsiteStats> hourlyStats = new ArrayList<>();
}
