package com.labMetricas.LabMetricas.auditLog.model.dto;

import java.time.LocalDateTime;

public class AuditLogDto {
    private String action;
    private LocalDateTime createdAt;
    private String userEmail;
    private String userName;

    public AuditLogDto(String action, LocalDateTime createdAt, String userEmail, String userName) {
        this.action = action;
        this.createdAt = createdAt;
        this.userEmail = userEmail;
        this.userName = userName;
    }

    public String getAction() { return action; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public String getUserEmail() { return userEmail; }
    public String getUserName() { return userName; }
} 