package com.labMetricas.LabMetricas.maintenance.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.UUID;

public class ScheduledMaintenanceRequestDto {
    @NotNull(message = "Equipment ID is required")
    private UUID equipmentId;

    @NotNull(message = "Maintenance Type ID is required")
    private UUID maintenanceTypeId;

    @NotNull(message = "Responsible User ID is required")
    private UUID responsibleUserId;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotNull(message = "Priority is required")
    private Priority priority = Priority.MEDIUM;

    @NotNull(message = "Frequency type is required")
    private FrequencyType frequencyType;

    @NotNull(message = "Frequency value is required")
    @Min(value = 1, message = "Frequency value must be at least 1")
    private Integer frequencyValue;

    @NotNull(message = "Next maintenance date is required")
    private LocalDateTime nextMaintenanceDate;

    // Priority levels
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // Frequency types
    public enum FrequencyType {
        DAILY, WEEKLY, MONTHLY, YEARLY
    }

    // Getters and Setters
    public UUID getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(UUID equipmentId) {
        this.equipmentId = equipmentId;
    }

    public UUID getMaintenanceTypeId() {
        return maintenanceTypeId;
    }

    public void setMaintenanceTypeId(UUID maintenanceTypeId) {
        this.maintenanceTypeId = maintenanceTypeId;
    }

    public UUID getResponsibleUserId() {
        return responsibleUserId;
    }

    public void setResponsibleUserId(UUID responsibleUserId) {
        this.responsibleUserId = responsibleUserId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
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