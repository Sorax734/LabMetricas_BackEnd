package com.labMetricas.LabMetricas.EquipmentCategory.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentCategoryDto {
    private UUID id;

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be less than 100 characters")
    private String name;

    private Boolean status;

    // Constructor without status (will default to true in service)
    public EquipmentCategoryDto(UUID id, String name) {
        this.id = id;
        this.name = name;
        this.status = true;
    }
} 