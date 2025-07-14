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

    private String address;
    private String phone;
    private String email;
    private String nif;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime lastModification;
} 