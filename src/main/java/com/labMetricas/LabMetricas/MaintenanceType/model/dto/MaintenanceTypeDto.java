package com.labMetricas.LabMetricas.MaintenanceType.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceTypeDto {
    private UUID id;
    private String name;
    private String description;
    private Boolean status = true;
} 