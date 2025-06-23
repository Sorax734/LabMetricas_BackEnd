package com.labMetricas.LabMetricas.MaintenanceProvider.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceProviderDto {
    private UUID id;

    @NotBlank(message = "Maintenance provider name is required")
    @Size(max = 50, message = "Maintenance provider name must be less than 50 characters")
    private String name;

    private Boolean status;

    // Constructor without status (will default to true in service)
    public MaintenanceProviderDto(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.status = true;
    }
} 