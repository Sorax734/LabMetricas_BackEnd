package com.labMetricas.LabMetricas.maintenance.model.dto;

import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
public class MaintenanceDetailDto {
    private UUID id;
    private String code;
    private String description;
    private Boolean status;
    private String priority;
    private String reviewStatus;
    private String rejectionReason;
    
    // Equipment Details
    private UUID equipmentId;
    private String equipmentName;
    private String equipmentCode;
    private String equipmentLocation;
    
    // Maintenance Type Details
    private UUID maintenanceTypeId;
    private String maintenanceTypeName;
    
    // Responsible User Details
    private UUID responsibleUserId;
    private String responsibleUserName;
    private String responsibleUserEmail;
    private String responsibleUserRole;
    
    // Request and Review Details
    private UUID requestedById;
    private String requestedByName;
    private String requestedByEmail;
    
    private UUID reviewedById;
    private String reviewedByName;
    private String reviewedByEmail;
    private LocalDateTime reviewedAt;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor to map from Maintenance entity
    public MaintenanceDetailDto(Maintenance maintenance) {
        this.id = maintenance.getId();
        this.code = maintenance.getCode();
        this.description = maintenance.getDescription();
        this.status = maintenance.getStatus();
        this.priority = maintenance.getPriority().name();
        this.reviewStatus = maintenance.getReviewStatus() != null ? maintenance.getReviewStatus().name() : null;
        this.rejectionReason = maintenance.getRejectionReason();
        
        // Equipment Details
        this.equipmentId = maintenance.getEquipment().getId();
        this.equipmentName = maintenance.getEquipment().getName();
        this.equipmentCode = maintenance.getEquipment().getCode();
        this.equipmentLocation = maintenance.getEquipment().getLocation();
        
        // Maintenance Type Details
        this.maintenanceTypeId = maintenance.getMaintenanceType().getId();
        this.maintenanceTypeName = maintenance.getMaintenanceType().getName();
        
        // Responsible User Details
        this.responsibleUserId = maintenance.getResponsible().getId();
        this.responsibleUserName = maintenance.getResponsible().getName();
        this.responsibleUserEmail = maintenance.getResponsible().getEmail();
        this.responsibleUserRole = maintenance.getResponsible().getRole().getName();
        
        // Request and Review Details
        if (maintenance.getRequestedBy() != null) {
            this.requestedById = maintenance.getRequestedBy().getId();
            this.requestedByName = maintenance.getRequestedBy().getName();
            this.requestedByEmail = maintenance.getRequestedBy().getEmail();
        }
        
        if (maintenance.getReviewedBy() != null) {
            this.reviewedById = maintenance.getReviewedBy().getId();
            this.reviewedByName = maintenance.getReviewedBy().getName();
            this.reviewedByEmail = maintenance.getReviewedBy().getEmail();
        }
        
        this.reviewedAt = maintenance.getReviewedAt();
        this.createdAt = maintenance.getCreatedAt();
        this.updatedAt = maintenance.getUpdatedAt();
    }
} 