package com.labMetricas.LabMetricas.maintenance.model.dto;

import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
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
    
    // Equipment Details
    private UUID equipmentId;
    private String equipmentName;
    private String equipmentCode;
    private String equipmentLocation;
    
    // Maintenance Type Details
    private UUID maintenanceTypeId;
    private String maintenanceTypeName;
    private Boolean isPreventive;
    private Boolean isCorrective;
    
    // Responsible User Details
    private UUID responsibleUserId;
    private String responsibleUserName;
    private String responsibleUserEmail;
    private String responsibleUserRole;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor to map from Maintenance entity
    public MaintenanceDetailDto(Maintenance maintenance) {
        this.id = maintenance.getId();
        this.code = maintenance.getCode();
        this.description = maintenance.getDescription();
        this.status = maintenance.getStatus();
        this.priority = maintenance.getPriority().name();
        
        // Equipment Details
        this.equipmentId = maintenance.getEquipment().getId();
        this.equipmentName = maintenance.getEquipment().getName();
        this.equipmentCode = maintenance.getEquipment().getCode();
        this.equipmentLocation = maintenance.getEquipment().getLocation();
        
        // Maintenance Type Details
        this.maintenanceTypeId = maintenance.getMaintenanceType().getId();
        this.maintenanceTypeName = maintenance.getMaintenanceType().getName();
        this.isPreventive = maintenance.getMaintenanceType().getIsPreventive();
        this.isCorrective = maintenance.getMaintenanceType().getIsCorrective();
        
        // Responsible User Details
        this.responsibleUserId = maintenance.getResponsible().getId();
        this.responsibleUserName = maintenance.getResponsible().getName();
        this.responsibleUserEmail = maintenance.getResponsible().getEmail();
        this.responsibleUserRole = maintenance.getResponsible().getRole().getName();
        
        this.createdAt = maintenance.getCreatedAt();
        this.updatedAt = maintenance.getUpdatedAt();
    }
} 