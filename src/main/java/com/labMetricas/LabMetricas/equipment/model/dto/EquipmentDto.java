package com.labMetricas.LabMetricas.equipment.model.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentDto {
    private UUID id;

    @NotBlank(message = "Equipment name is required")
    @Size(max = 100, message = "Equipment name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Equipment code is required")
    @Size(max = 10, message = "Equipment code must be less than 10 characters")
    private String code;

    @NotBlank(message = "Serial number is required")
    @Size(max = 24, message = "Serial number must be less than 24 characters")
    private String serialNumber;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Brand is required")
    @Size(max = 50, message = "Brand must be less than 50 characters")
    private String brand;

    @NotBlank(message = "Model is required")
    @Size(max = 100, message = "Model must be less than 100 characters")
    private String model;

    private String remarks;

    private LocalDateTime updatedAt;

    private Boolean status = true;

    @NotNull(message = "Assigned user ID is required")
    private UUID assignedToId;

    @NotNull(message = "Equipment category ID is required")
    private UUID equipmentCategoryId;

    @NotNull(message = "Maintenance provider ID is required")
    private UUID maintenanceProviderId;
} 