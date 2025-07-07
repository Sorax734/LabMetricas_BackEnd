package com.labMetricas.LabMetricas.MaintenanceType.service;

import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import com.labMetricas.LabMetricas.MaintenanceType.model.dto.MaintenanceTypeDto;
import com.labMetricas.LabMetricas.MaintenanceType.repository.MaintenanceTypeRepository;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaintenanceTypeService {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceTypeService.class);

    @Autowired
    private MaintenanceTypeRepository maintenanceTypeRepository;

    // Convert MaintenanceType to MaintenanceTypeDto
    private MaintenanceTypeDto convertToDto(MaintenanceType maintenanceType) {
        return new MaintenanceTypeDto(
            maintenanceType.getId(),
            maintenanceType.getName(),
            maintenanceType.getDescription(),
            maintenanceType.getStatus()
        );
    }

    // Create a new Maintenance Type
    @Transactional
    public ResponseEntity<ResponseObject> createMaintenanceType(MaintenanceTypeDto maintenanceTypeDto) {
        try {
            // Check if maintenance type with same name already exists
            if (maintenanceTypeRepository.findByName(maintenanceTypeDto.getName()).isPresent()) {
                return ResponseEntity.badRequest().body(
                    new ResponseObject("Maintenance Type with this name already exists", null, TypeResponse.ERROR)
                );
            }

            // Create new maintenance type
            MaintenanceType maintenanceType = new MaintenanceType(
                maintenanceTypeDto.getName(), 
                maintenanceTypeDto.getDescription()
            );
            maintenanceType.setStatus(true);

            // Save maintenance type
            MaintenanceType savedMaintenanceType = maintenanceTypeRepository.save(maintenanceType);

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject("Maintenance Type created successfully", 
                    convertToDto(savedMaintenanceType), 
                    TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error creating maintenance type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error creating maintenance type", null, TypeResponse.ERROR)
            );
        }
    }

    // Update an existing Maintenance Type
    @Transactional
    public ResponseEntity<ResponseObject> updateMaintenanceType(MaintenanceTypeDto maintenanceTypeDto) {
        try {
            // Find existing maintenance type
            MaintenanceType existingMaintenanceType = maintenanceTypeRepository.findById(maintenanceTypeDto.getId())
                .orElseThrow(() -> new RuntimeException("Maintenance Type not found"));

            // Check if name is being changed to an existing name
            if (!existingMaintenanceType.getName().equals(maintenanceTypeDto.getName())) {
                if (maintenanceTypeRepository.findByName(maintenanceTypeDto.getName()).isPresent()) {
                    return ResponseEntity.badRequest().body(
                        new ResponseObject("Maintenance Type with this name already exists", null, TypeResponse.ERROR)
                    );
                }
            }

            // Update maintenance type details
            existingMaintenanceType.setName(maintenanceTypeDto.getName());
            existingMaintenanceType.setDescription(maintenanceTypeDto.getDescription());

            // Save updated maintenance type
            MaintenanceType updatedMaintenanceType = maintenanceTypeRepository.save(existingMaintenanceType);

            return ResponseEntity.ok(
                new ResponseObject("Maintenance Type updated successfully", 
                    convertToDto(updatedMaintenanceType), 
                    TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error updating maintenance type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error updating maintenance type", null, TypeResponse.ERROR)
            );
        }
    }

    // Get Maintenance Type by ID
    public ResponseEntity<ResponseObject> getMaintenanceTypeById(UUID id) {
        try {
            MaintenanceType maintenanceType = maintenanceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance Type not found"));

            return ResponseEntity.ok(
                new ResponseObject("Maintenance Type retrieved successfully", 
                    convertToDto(maintenanceType), 
                    TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving maintenance type", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("Maintenance Type not found", null, TypeResponse.ERROR)
            );
        }
    }

    // Get all Maintenance Types (including inactive)
    public ResponseEntity<ResponseObject> getAllMaintenanceTypes() {
        try {
            List<MaintenanceTypeDto> maintenanceTypes = maintenanceTypeRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                new ResponseObject("All Maintenance Types retrieved successfully", 
                    maintenanceTypes, 
                    TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving maintenance types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving maintenance types", null, TypeResponse.ERROR)
            );
        }
    }

    // Logical delete (toggle status)
    @Transactional
    public ResponseEntity<ResponseObject> toggleMaintenanceTypeStatus(UUID id) {
        try {
            MaintenanceType maintenanceType = maintenanceTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maintenance Type not found"));

            // Toggle status
            maintenanceType.setStatus(!maintenanceType.getStatus());

            // Save updated maintenance type
            MaintenanceType updatedMaintenanceType = maintenanceTypeRepository.save(maintenanceType);

            return ResponseEntity.ok(
                new ResponseObject("Maintenance Type status updated successfully", 
                    convertToDto(updatedMaintenanceType), 
                    TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error toggling maintenance type status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error toggling maintenance type status", null, TypeResponse.ERROR)
            );
        }
    }
} 