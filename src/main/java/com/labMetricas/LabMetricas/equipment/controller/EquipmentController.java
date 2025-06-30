package com.labMetricas.LabMetricas.equipment.controller;

import com.labMetricas.LabMetricas.equipment.model.dto.EquipmentDto;
import com.labMetricas.LabMetricas.equipment.service.EquipmentService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.util.PageResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    // Create new equipment
    @PostMapping
    public ResponseEntity<ResponseObject> createEquipment(@Valid @RequestBody EquipmentDto equipmentDto) {
        EquipmentDto createdEquipment = equipmentService.createEquipment(equipmentDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseObject("Equipment created successfully", createdEquipment, TypeResponse.SUCCESS));
    }

    // Get equipment by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getEquipmentById(@PathVariable UUID id) {
        EquipmentDto equipment = equipmentService.getEquipmentById(id);
        return ResponseEntity.ok(
                new ResponseObject("Equipment retrieved successfully", equipment, TypeResponse.SUCCESS));
    }

    // Get all equipment
    @GetMapping
    public ResponseEntity<ResponseObject> getAllEquipment(
            @RequestParam(defaultValue = "false") boolean activeOnly
    ) {
        List<EquipmentDto> equipmentList = activeOnly 
                ? equipmentService.getActiveEquipment()
                : equipmentService.getAllEquipment();
        
        return ResponseEntity.ok(
                new ResponseObject("Equipment list retrieved successfully", 
                    equipmentList, 
                    TypeResponse.SUCCESS));
    }

    // Update equipment
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateEquipment(
            @PathVariable UUID id, 
            @Valid @RequestBody EquipmentDto equipmentDto
    ) {
        EquipmentDto updatedEquipment = equipmentService.updateEquipment(id, equipmentDto);
        return ResponseEntity.ok(
                new ResponseObject("Equipment updated successfully", updatedEquipment, TypeResponse.SUCCESS));
    }

    // Toggle equipment status
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ResponseObject> toggleEquipmentStatus(@PathVariable UUID id) {
        EquipmentDto updatedEquipment = equipmentService.toggleEquipmentStatus(id);
        return ResponseEntity.ok(
                new ResponseObject("Equipment status toggled successfully", updatedEquipment, TypeResponse.SUCCESS));
    }

    // Soft delete equipment
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteEquipment(@PathVariable UUID id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.ok(
                new ResponseObject("Equipment soft deleted successfully", null, TypeResponse.SUCCESS));
    }

    // Restore soft-deleted equipment this isn't actually necesary, xd
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ResponseObject> restoreEquipment(@PathVariable UUID id) {
        EquipmentDto restoredEquipment = equipmentService.restoreEquipment(id);
        return ResponseEntity.ok(
                new ResponseObject("Equipment restored successfully", restoredEquipment, TypeResponse.SUCCESS));
    }
} 