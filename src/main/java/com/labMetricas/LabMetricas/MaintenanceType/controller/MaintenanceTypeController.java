package com.labMetricas.LabMetricas.MaintenanceType.controller;

import com.labMetricas.LabMetricas.MaintenanceType.model.dto.MaintenanceTypeDto;
import com.labMetricas.LabMetricas.MaintenanceType.service.MaintenanceTypeService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance-types")
public class MaintenanceTypeController {

    @Autowired
    private MaintenanceTypeService maintenanceTypeService;

    @PostMapping
    public ResponseEntity<ResponseObject> createMaintenanceType(@RequestBody MaintenanceTypeDto maintenanceTypeDto) {
        return maintenanceTypeService.createMaintenanceType(maintenanceTypeDto);
    }

    @PutMapping
    public ResponseEntity<ResponseObject> updateMaintenanceType(@RequestBody MaintenanceTypeDto maintenanceTypeDto) {
        return maintenanceTypeService.updateMaintenanceType(maintenanceTypeDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getMaintenanceTypeById(@PathVariable UUID id) {
        return maintenanceTypeService.getMaintenanceTypeById(id);
    }

    @GetMapping
    public ResponseEntity<ResponseObject> getAllMaintenanceTypes() {
        return maintenanceTypeService.getAllMaintenanceTypes();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> toggleMaintenanceTypeStatus(@PathVariable UUID id) {
        return maintenanceTypeService.toggleMaintenanceTypeStatus(id);
    }
} 