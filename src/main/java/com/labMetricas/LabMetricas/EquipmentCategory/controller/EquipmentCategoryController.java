package com.labMetricas.LabMetricas.EquipmentCategory.controller;

import com.labMetricas.LabMetricas.EquipmentCategory.model.dto.EquipmentCategoryDto;
import com.labMetricas.LabMetricas.EquipmentCategory.service.EquipmentCategoryService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/equipment-categories")
@RequiredArgsConstructor
public class EquipmentCategoryController {
    private final EquipmentCategoryService equipmentCategoryService;

    // Create a new Equipment Category
    @PostMapping
    public ResponseEntity<ResponseObject> createEquipmentCategory(
            @Valid @RequestBody EquipmentCategoryDto categoryDto) {
        ResponseObject response = equipmentCategoryService.createEquipmentCategory(categoryDto);
        return ResponseEntity.ok(response);
    }

    // Get all Equipment Categories
    @GetMapping
    public ResponseEntity<ResponseObject> getAllEquipmentCategories() {
        ResponseObject response = equipmentCategoryService.getAllEquipmentCategories();
        return ResponseEntity.ok(response);
    }

    // Get active Equipment Categories
    @GetMapping("/active")
    public ResponseEntity<ResponseObject> getActiveEquipmentCategories() {
        ResponseObject response = equipmentCategoryService.getActiveEquipmentCategories();
        return ResponseEntity.ok(response);
    }

    // Get Equipment Category by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getEquipmentCategoryById(@PathVariable UUID id) {
        ResponseObject response = equipmentCategoryService.getEquipmentCategoryById(id);
        return ResponseEntity.ok(response);
    }

    // Update Equipment Category
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateEquipmentCategory(
            @PathVariable UUID id, 
            @Valid @RequestBody EquipmentCategoryDto categoryDto) {
        ResponseObject response = equipmentCategoryService.updateEquipmentCategory(id, categoryDto);
        return ResponseEntity.ok(response);
    }

    // Toggle Equipment Category status
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ResponseObject> toggleEquipmentCategoryStatus(@PathVariable UUID id) {
        ResponseObject response = equipmentCategoryService.toggleEquipmentCategoryStatus(id);
        return ResponseEntity.ok(response);
    }

    // Delete Equipment Category (soft delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseObject> deleteEquipmentCategory(@PathVariable UUID id) {
        ResponseObject response = equipmentCategoryService.deleteEquipmentCategory(id);
        return ResponseEntity.ok(response);
    }
} 