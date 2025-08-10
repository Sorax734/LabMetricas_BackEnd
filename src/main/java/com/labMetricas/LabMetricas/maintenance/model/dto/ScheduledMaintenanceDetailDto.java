package com.labMetricas.LabMetricas.maintenance.model.dto;

import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.maintenance.model.ScheduledMaintenance;
import com.labMetricas.LabMetricas.maintenance.model.FrequencyType;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import com.labMetricas.LabMetricas.user.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ScheduledMaintenanceDetailDto {
    private UUID id;
    private String code;
    private String description;
    private Boolean status;
    private Priority priority;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
    private String reviewStatus;
    private String rejectionReason;

    // Equipment details
    private UUID equipmentId;
    private String equipmentName;
    private String equipmentCode;
    
    // Maintenance type details
    private UUID maintenanceTypeId;
    private String maintenanceTypeName;
    
    // Responsible user details
    private UUID responsibleUserId;
    private String responsibleUserName;
    private String responsibleUserEmail;
    
    // Scheduled maintenance specific fields
    private FrequencyType frequencyType;
    private Integer frequencyValue;
    private LocalDateTime nextMaintenanceDate;

    // Request and Review Details
    private UUID requestedById;
    private String requestedByName;
    private String requestedByEmail;

    private UUID reviewedById;
    private String reviewedByName;
    private String reviewedByEmail;
    private LocalDateTime reviewedAt;

    public ScheduledMaintenanceDetailDto() {}

    public ScheduledMaintenanceDetailDto(Maintenance maintenance, ScheduledMaintenance scheduledMaintenance) {
        this.id = maintenance.getId();
        this.code = maintenance.getCode();
        this.description = maintenance.getDescription();
        this.status = maintenance.getStatus();
        this.priority = Priority.valueOf(maintenance.getPriority().name());
        this.createdAt = maintenance.getCreatedAt();
        this.updatedAt = maintenance.getUpdatedAt();
        this.deletedAt = maintenance.getDeletedAt();
        this.reviewStatus = maintenance.getReviewStatus() != null ? maintenance.getReviewStatus().name() : null;
        this.rejectionReason = maintenance.getRejectionReason();

        // Equipment details
        Equipment equipment = maintenance.getEquipment();
        if (equipment != null) {
            this.equipmentId = equipment.getId();
            this.equipmentName = equipment.getName();
            this.equipmentCode = equipment.getCode();
        }
        
        // Maintenance type details
        MaintenanceType maintenanceType = maintenance.getMaintenanceType();
        if (maintenanceType != null) {
            this.maintenanceTypeId = maintenanceType.getId();
            this.maintenanceTypeName = maintenanceType.getName();
        }
        
        // Responsible user details
        User responsible = maintenance.getResponsible();
        if (responsible != null) {
            this.responsibleUserId = responsible.getId();
            this.responsibleUserName = responsible.getName();
            this.responsibleUserEmail = responsible.getEmail();
        }
        
        // Scheduled maintenance specific details
        if (scheduledMaintenance != null) {
            this.frequencyType = scheduledMaintenance.getFrequencyType();
            this.frequencyValue = scheduledMaintenance.getFrequencyValue().intValue();
            this.nextMaintenanceDate = scheduledMaintenance.getNextMaintenance();
        }

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
    }



    // Priority enum
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }



    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public UUID getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(UUID equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public UUID getMaintenanceTypeId() {
        return maintenanceTypeId;
    }

    public void setMaintenanceTypeId(UUID maintenanceTypeId) {
        this.maintenanceTypeId = maintenanceTypeId;
    }

    public String getMaintenanceTypeName() {
        return maintenanceTypeName;
    }

    public void setMaintenanceTypeName(String maintenanceTypeName) {
        this.maintenanceTypeName = maintenanceTypeName;
    }

    public UUID getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(UUID responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    public String getResponsibleUserName() {
        return responsibleUserName;
    }

    public void setResponsibleUserName(String responsibleUserName) {
        this.responsibleUserName = responsibleUserName;
    }

    public String getResponsibleUserEmail() {
        return responsibleUserEmail;
    }

    public void setResponsibleUserEmail(String responsibleUserEmail) {
        this.responsibleUserEmail = responsibleUserEmail;
    }

    public FrequencyType getFrequencyType() {
        return frequencyType;
    }

    public void setFrequencyType(FrequencyType frequencyType) {
        this.frequencyType = frequencyType;
    }

    public Integer getFrequencyValue() {
        return frequencyValue;
    }

    public void setFrequencyValue(Integer frequencyValue) {
        this.frequencyValue = frequencyValue;
    }

    public LocalDateTime getNextMaintenanceDate() {
        return nextMaintenanceDate;
    }

    public void setNextMaintenanceDate(LocalDateTime nextMaintenanceDate) {
        this.nextMaintenanceDate = nextMaintenanceDate;
    }
} 