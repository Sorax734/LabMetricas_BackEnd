package com.labMetricas.LabMetricas.maintenance.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class MaintenanceRequestDto {
    @NotNull(message = "Equipment ID is required")
    private UUID equipmentId;

    @NotNull(message = "Maintenance Type ID is required")
    private UUID maintenanceTypeId;

    @NotNull(message = "Responsible User ID is required")
    private UUID responsibleUserId;

    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    // Priority levels
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    @NotNull(message = "Priority is required")
    private Priority priority = Priority.MEDIUM;

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
} 