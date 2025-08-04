package com.labMetricas.LabMetricas.maintenance.model.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class MaintenanceSubmitForReviewDto {
    @NotNull(message = "Maintenance ID is required")
    private UUID maintenanceId;

    // Getters and Setters
    public UUID getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(UUID maintenanceId) {
        this.maintenanceId = maintenanceId;
    }
} 