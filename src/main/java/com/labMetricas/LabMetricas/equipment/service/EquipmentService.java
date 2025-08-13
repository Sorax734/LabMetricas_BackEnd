package com.labMetricas.LabMetricas.equipment.service;

import com.labMetricas.LabMetricas.EquipmentCategory.model.EquipmentCategory;
import com.labMetricas.LabMetricas.EquipmentCategory.repository.EquipmentCategoryRepository;
import com.labMetricas.LabMetricas.MaintenanceProvider.model.MaintenanceProvider;
import com.labMetricas.LabMetricas.MaintenanceProvider.repository.MaintenanceProviderRepository;
import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import com.labMetricas.LabMetricas.MaintenanceType.repository.MaintenanceTypeRepository;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.equipment.controller.EquipmentController;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.equipment.model.dto.EquipmentDto;
import com.labMetricas.LabMetricas.equipment.repository.EquipmentRepository;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.maintenance.model.ScheduledMaintenance;
import com.labMetricas.LabMetricas.maintenance.model.dto.ScheduledMaintenanceRequestDto;
import com.labMetricas.LabMetricas.maintenance.repository.MaintenanceRepository;
import com.labMetricas.LabMetricas.maintenance.repository.ScheduledMaintenanceRepository;
import com.labMetricas.LabMetricas.maintenance.service.ScheduledMaintenanceService;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ScheduledMaintenanceRepository scheduledMaintenanceRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private MaintenanceTypeRepository maintenanceTypeRepository;

    @Autowired
    private EquipmentCategoryRepository equipmentCategoryRepository;

    @Autowired
    private MaintenanceProviderRepository maintenanceProviderRepository;

    @Autowired
    private ScheduledMaintenanceService scheduledMaintenanceService;

    // Create new equipment
    @Transactional
    public EquipmentDto createEquipment(EquipmentDto equipmentDto) {
        Equipment equipment = mapToEntity(equipmentDto);
        equipment.setCreatedAt(LocalDateTime.now());
        equipment.setStatus(true);
        Equipment savedEquipment = equipmentRepository.save(equipment);
        return mapToDto(savedEquipment);
    }

    // Create new equipment
    @Transactional
    public EquipmentDto createEquipmentWithMaintenances(EquipmentController.EquipmentWithMaintenancesDto payload, User currentUser) {
        // Log current user information
        System.out.println("Creating equipment with maintenances - CurrentUser: " + 
            (currentUser != null ? currentUser.getEmail() + " (ID: " + currentUser.getId() + ")" : "NULL"));
        
        Equipment equipment = mapToEntity(payload.equipment());
        equipment.setCreatedAt(LocalDateTime.now());
        equipment.setStatus(true);
        Equipment savedEquipment = equipmentRepository.saveAndFlush(equipment);

        for (ScheduledMaintenanceRequestDto dto : payload.maintenances()) {
            Maintenance maintenance = new Maintenance();

            MaintenanceType maintenanceType = maintenanceTypeRepository.findById(dto.getMaintenanceTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("Maintenance Type not found"));

            User responsible = userRepository.findByIdWithRole(dto.getResponsibleUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Responsible User not found"));

            maintenance.setDescription(dto.getDescription());
            maintenance.setEquipment(savedEquipment);
            maintenance.setMaintenanceType(maintenanceType);
            maintenance.setResponsible(responsible);
            maintenance.setRequestedBy(currentUser); // Set the creator
            maintenance.setCode(scheduledMaintenanceService.generateMaintenanceCode(maintenanceType, true));
            maintenance.setCreatedAt(LocalDateTime.now());
            maintenance.setStatus(true);
            maintenance.setPriority(scheduledMaintenanceService.convertPriority(dto.getPriority()));

            Maintenance savedMaintenance = maintenanceRepository.saveAndFlush(maintenance);
            
            // Log to verify requestedBy was saved
            System.out.println("Saved maintenance - requestedBy: " + 
                (savedMaintenance.getRequestedBy() != null ? 
                    savedMaintenance.getRequestedBy().getEmail() + " (ID: " + savedMaintenance.getRequestedBy().getId() + ")" : 
                    "NULL"));

            // Create scheduled maintenance
            ScheduledMaintenance scheduledMaintenance = new ScheduledMaintenance();
            scheduledMaintenance.setMaintenance(savedMaintenance);
            scheduledMaintenance.setNextMaintenance(dto.getNextMaintenanceDate());
            scheduledMaintenance.setFrequencyType(scheduledMaintenanceService.convertFrequencyType(dto.getFrequencyType()));
            scheduledMaintenance.setFrequencyValue(dto.getFrequencyValue().shortValue());

            // Save scheduled maintenance
            scheduledMaintenanceRepository.save(scheduledMaintenance);

            // Create audit log
            scheduledMaintenanceService.createScheduledMaintenanceAuditLog(savedMaintenance, currentUser);

            // Send notification
            scheduledMaintenanceService.sendScheduledMaintenanceNotification(savedMaintenance, responsible);
        }

        return mapToDto(savedEquipment);
    }

    // Get equipment by ID
    public EquipmentDto getEquipmentById(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found with id: " + id));
        return mapToDto(equipment);
    }

    // Get all equipment (including soft-deleted)
    public List<EquipmentDto> getAllEquipment() {
        return equipmentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Get all active equipment
    public List<EquipmentDto> getActiveEquipment() {
        return equipmentRepository.findAll()
                .stream()
                .filter(e -> e.getStatus() != null && e.getStatus() && e.getDeletedAt() == null)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Update equipment
    @Transactional
    public EquipmentDto updateEquipment(UUID id, EquipmentDto equipmentDto) {
        Equipment existingEquipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found with id: " + id));

        // Update fields
        existingEquipment.setName(equipmentDto.getName());
        existingEquipment.setCode(equipmentDto.getCode());
        existingEquipment.setSerialNumber(equipmentDto.getSerialNumber());
        existingEquipment.setLocation(equipmentDto.getLocation());
        existingEquipment.setBrand(equipmentDto.getBrand());
        existingEquipment.setModel(equipmentDto.getModel());
        existingEquipment.setRemarks(equipmentDto.getRemarks());
        existingEquipment.setUpdatedAt(LocalDateTime.now());

        // Update related entities
        existingEquipment.setAssignedTo(userRepository.findById(equipmentDto.getAssignedToId())
                .orElseThrow(() -> new EntityNotFoundException("User not found")));
        existingEquipment.setEquipmentCategory(equipmentCategoryRepository.findById(equipmentDto.getEquipmentCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Equipment Category not found")));
        existingEquipment.setMaintenanceProvider(maintenanceProviderRepository.findById(equipmentDto.getMaintenanceProviderId())
                .orElseThrow(() -> new EntityNotFoundException("Maintenance Provider not found")));

        Equipment updatedEquipment = equipmentRepository.save(existingEquipment);
        return mapToDto(updatedEquipment);
    }

    // Toggle equipment status (active/inactive)
    @Transactional
    public EquipmentDto toggleEquipmentStatus(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found with id: " + id));

        equipment.setStatus(!equipment.getStatus());
        equipment.setUpdatedAt(LocalDateTime.now());

        Equipment updatedEquipment = equipmentRepository.save(equipment);
        return mapToDto(updatedEquipment);
    }

    // Soft delete equipment
    @Transactional
    public void deleteEquipment(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found with id: " + id));

        equipment.setStatus(false);
        equipment.setDeletedAt(LocalDateTime.now());
        equipmentRepository.save(equipment);
    }

    // Restore soft-deleted equipment
    @Transactional
    public EquipmentDto restoreEquipment(UUID id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found with id: " + id));

        equipment.setStatus(true);
        equipment.setDeletedAt(null);
        Equipment restoredEquipment = equipmentRepository.save(equipment);
        return mapToDto(restoredEquipment);
    }

    // Mapping methods
    private Equipment mapToEntity(EquipmentDto dto) {
        Equipment equipment = new Equipment();
        equipment.setName(dto.getName());
        equipment.setCode(dto.getCode());
        equipment.setSerialNumber(dto.getSerialNumber());
        equipment.setLocation(dto.getLocation());
        equipment.setBrand(dto.getBrand());
        equipment.setModel(dto.getModel());
        equipment.setRemarks(dto.getRemarks());
        equipment.setStatus(dto.getStatus());

        // Set related entities
        equipment.setAssignedTo(userRepository.findById(dto.getAssignedToId())
                .orElseThrow(() -> new EntityNotFoundException("User not found")));
        equipment.setEquipmentCategory(equipmentCategoryRepository.findById(dto.getEquipmentCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Equipment Category not found")));
        equipment.setMaintenanceProvider(maintenanceProviderRepository.findById(dto.getMaintenanceProviderId())
                .orElseThrow(() -> new EntityNotFoundException("Maintenance Provider not found")));

        return equipment;
    }

    private EquipmentDto mapToDto(Equipment equipment) {
        EquipmentDto dto = new EquipmentDto();
        dto.setId(equipment.getId());
        dto.setName(equipment.getName());
        dto.setCode(equipment.getCode());
        dto.setSerialNumber(equipment.getSerialNumber());
        dto.setLocation(equipment.getLocation());
        dto.setBrand(equipment.getBrand());
        dto.setModel(equipment.getModel());
        dto.setRemarks(equipment.getRemarks());
        dto.setStatus(equipment.getStatus());
        dto.setUpdatedAt(equipment.getUpdatedAt());
        dto.setAssignedToId(equipment.getAssignedTo().getId());
        dto.setEquipmentCategoryId(equipment.getEquipmentCategory().getId());
        dto.setMaintenanceProviderId(equipment.getMaintenanceProvider().getId());

        return dto;
    }
} 