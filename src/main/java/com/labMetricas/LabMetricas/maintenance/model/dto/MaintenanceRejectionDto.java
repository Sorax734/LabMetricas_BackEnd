package com.labMetricas.LabMetricas.maintenance.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MaintenanceRejectionDto {
    @NotNull(message = "Rejection reason is required")
    @Size(min = 1, max = 500, message = "Rejection reason must be between 1 and 500 characters")
    private String rejectionReason;

    // Getters and Setters
    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
} 