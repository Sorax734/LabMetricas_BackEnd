package com.labMetricas.LabMetricas.Notice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.Company.model.Company;
import com.labMetricas.LabMetricas.NoticeRecipient.model.NoticeRecipient;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "notices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notice {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "title", columnDefinition = "VARCHAR(120)", nullable = false, length = 120)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "target_url", columnDefinition = "VARCHAR(255)", nullable = false, length = 255)
    private String targetUrl;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "title_color", columnDefinition = "VARCHAR(7)", length = 7)
    private String titleColor = "#000000";

    @Column(name = "description_color", columnDefinition = "VARCHAR(7)", length = 7)
    private String descriptionColor = "#666666";

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('ACTIVE', 'INACTIVE', 'SCHEDULED')")
    private NoticeStatus status = NoticeStatus.SCHEDULED;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "notice")
    @JsonIgnore
    private List<NoticeRecipient> recipients;

    public enum NoticeStatus {
        ACTIVE, INACTIVE, SCHEDULED
    }
}