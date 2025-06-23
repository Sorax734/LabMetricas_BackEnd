package com.labMetricas.LabMetricas.MaintenanceProvider.service;

import com.labMetricas.LabMetricas.MaintenanceProvider.model.MaintenanceProvider;
import com.labMetricas.LabMetricas.MaintenanceProvider.model.dto.MaintenanceProviderDto;
import com.labMetricas.LabMetricas.MaintenanceProvider.repository.MaintenanceProviderRepository;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceProviderService {
    private final MaintenanceProviderRepository maintenanceProviderRepository;

    // Create a new Maintenance Provider (always set status to true)
    @Transactional
    public ResponseObject createMaintenanceProvider(MaintenanceProviderDto providerDto) {
        // Check if maintenance provider with same name already exists
        if (maintenanceProviderRepository.existsByNameIgnoreCase(providerDto.getName())) {
            return new ResponseObject("Maintenance provider with this name already exists", TypeResponse.ERROR);
        }

        // Create new maintenance provider (always set status to true)
        MaintenanceProvider provider = new MaintenanceProvider();
        provider.setName(providerDto.getName());
        provider.setStatus(true);  // Always set to true when creating

        MaintenanceProvider savedProvider = maintenanceProviderRepository.save(provider);

        // Convert to DTO for response
        MaintenanceProviderDto savedDto = convertToDto(savedProvider);
        return new ResponseObject("Maintenance provider created successfully", savedDto, TypeResponse.SUCCESS);
    }

    // Toggle Maintenance Provider status
    @Transactional
    public ResponseObject toggleMaintenanceProviderStatus(UUID id) {
        MaintenanceProvider provider = maintenanceProviderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance provider not found with id: " + id));

        // Toggle the status
        provider.setStatus(!provider.getStatus());
        MaintenanceProvider updatedProvider = maintenanceProviderRepository.save(provider);

        return new ResponseObject("Maintenance provider status toggled successfully", convertToDto(updatedProvider), TypeResponse.SUCCESS);
    }

    // Get all Maintenance Providers
    @Transactional(readOnly = true)
    public ResponseObject getAllMaintenanceProviders() {
        List<MaintenanceProviderDto> providers = maintenanceProviderRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseObject("Maintenance providers retrieved successfully", providers, TypeResponse.SUCCESS);
    }

    // Get active Maintenance Providers
    @Transactional(readOnly = true)
    public ResponseObject getActiveMaintenanceProviders() {
        List<MaintenanceProviderDto> activeProviders = maintenanceProviderRepository.findByStatusTrue()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        return new ResponseObject("Active maintenance providers retrieved successfully", activeProviders, TypeResponse.SUCCESS);
    }

    // Get Maintenance Provider by ID
    @Transactional(readOnly = true)
    public ResponseObject getMaintenanceProviderById(UUID id) {
        MaintenanceProvider provider = maintenanceProviderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance provider not found with id: " + id));
        return new ResponseObject("Maintenance provider retrieved successfully", convertToDto(provider), TypeResponse.SUCCESS);
    }

    // Update Maintenance Provider (allow updating name, but not status directly)
    @Transactional
    public ResponseObject updateMaintenanceProvider(UUID id, MaintenanceProviderDto providerDto) {
        MaintenanceProvider existingProvider = maintenanceProviderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance provider not found with id: " + id));

        // Check if new name already exists (excluding current provider)
        if (maintenanceProviderRepository.findByNameIgnoreCase(providerDto.getName())
                .map(prov -> !prov.getId().equals(id))
                .orElse(false)) {
            return new ResponseObject("Maintenance provider with this name already exists", TypeResponse.ERROR);
        }

        // Update only the name
        existingProvider.setName(providerDto.getName());

        MaintenanceProvider updatedProvider = maintenanceProviderRepository.save(existingProvider);
        return new ResponseObject("Maintenance provider updated successfully", convertToDto(updatedProvider), TypeResponse.SUCCESS);
    }

    // Delete Maintenance Provider (soft delete)
    @Transactional
    public ResponseObject deleteMaintenanceProvider(UUID id) {
        MaintenanceProvider provider = maintenanceProviderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Maintenance provider not found with id: " + id));

        provider.setStatus(false);
        maintenanceProviderRepository.save(provider);
        return new ResponseObject("Maintenance provider soft deleted successfully", TypeResponse.SUCCESS);
    }

    // Utility method to convert Entity to DTO
    private MaintenanceProviderDto convertToDto(MaintenanceProvider provider) {
        return new MaintenanceProviderDto(
                provider.getId(),
                provider.getName(),
                provider.getStatus()
        );
    }
} 