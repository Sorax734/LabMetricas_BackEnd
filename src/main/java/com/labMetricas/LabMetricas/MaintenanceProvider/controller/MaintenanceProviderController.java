package com.labMetricas.LabMetricas.MaintenanceProvider.controller;

import com.labMetricas.LabMetricas.MaintenanceProvider.model.dto.MaintenanceProviderDto;
import com.labMetricas.LabMetricas.MaintenanceProvider.service.MaintenanceProviderService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance-providers")
@RequiredArgsConstructor
public class MaintenanceProviderController {
    private final MaintenanceProviderService maintenanceProviderService;

    // Create a new Maintenance Provider
    @PostMapping
    public ResponseEntity<ResponseObject> createMaintenanceProvider(
            @Valid @RequestBody MaintenanceProviderDto providerDto) {
        ResponseObject response = maintenanceProviderService.createMaintenanceProvider(providerDto);
        return ResponseEntity.ok(response);
    }

    // Get all Maintenance Providers
    @GetMapping
    public ResponseEntity<ResponseObject> getAllMaintenanceProviders() {
        ResponseObject response = maintenanceProviderService.getAllMaintenanceProviders();
        return ResponseEntity.ok(response);
    }

    // Get active Maintenance Providers
    @GetMapping("/active")
    public ResponseEntity<ResponseObject> getActiveMaintenanceProviders() {
        ResponseObject response = maintenanceProviderService.getActiveMaintenanceProviders();
        return ResponseEntity.ok(response);
    }

    // Get Maintenance Provider by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResponseObject> getMaintenanceProviderById(@PathVariable UUID id) {
        ResponseObject response = maintenanceProviderService.getMaintenanceProviderById(id);
        return ResponseEntity.ok(response);
    }

    // Update Maintenance Provider name
    @PutMapping("/{id}")
    public ResponseEntity<ResponseObject> updateMaintenanceProvider(
            @PathVariable UUID id, 
            @Valid @RequestBody MaintenanceProviderDto providerDto) {
        ResponseObject response = maintenanceProviderService.updateMaintenanceProvider(id, providerDto);
        return ResponseEntity.ok(response);
    }

    // Toggle Maintenance Provider status
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<ResponseObject> toggleMaintenanceProviderStatus(@PathVariable UUID id) {
        ResponseObject response = maintenanceProviderService.toggleMaintenanceProviderStatus(id);
        return ResponseEntity.ok(response);
    }
} 