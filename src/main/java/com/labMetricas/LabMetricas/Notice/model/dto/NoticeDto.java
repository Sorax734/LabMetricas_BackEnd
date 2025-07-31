package com.labMetricas.LabMetricas.Notice.model.dto;

import com.labMetricas.LabMetricas.Notice.model.Notice;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class NoticeDto {
    private UUID id;
    private String title;
    private String description;
    private Boolean status;
    private String createdByEmail;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    public NoticeDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.description = notice.getDescription();
        this.status = notice.getStatus();
        this.createdByEmail = notice.getCreatedBy() != null ? notice.getCreatedBy().getEmail() : null;
        this.createdByName = notice.getCreatedBy() != null ? notice.getCreatedBy().getName() : null;
        this.createdAt = notice.getCreatedAt();
        this.updatedAt = notice.getUpdatedAt();
        this.deletedAt = notice.getDeletedAt();
    }
} 