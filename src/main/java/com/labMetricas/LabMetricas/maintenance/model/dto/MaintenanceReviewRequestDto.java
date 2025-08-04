package com.labMetricas.LabMetricas.maintenance.model.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class MaintenanceReviewRequestDto {
    @NotNull(message = "Maintenance ID is required")
    private UUID maintenanceId;

    private String rejectionReason;

    // Getters and Setters
    public UUID getMaintenanceId() {
        return maintenanceId;
    }

    public void setMaintenanceId(UUID maintenanceId) {
        this.maintenanceId = maintenanceId;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
} 