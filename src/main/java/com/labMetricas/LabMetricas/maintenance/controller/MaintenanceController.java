package com.labMetricas.LabMetricas.maintenance.controller;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.equipment.repository.EquipmentRepository;
import com.labMetricas.LabMetricas.maintenance.model.MaintenanceType;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceInitDataDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceRequestDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceDetailDto;
import com.labMetricas.LabMetricas.maintenance.repository.MaintenanceTypeRepository;
import com.labMetricas.LabMetricas.maintenance.repository.ScheduledMaintenanceRepository;
import com.labMetricas.LabMetricas.maintenance.service.MaintenanceService;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    @Autowired
    private MaintenanceService maintenanceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaintenanceTypeRepository maintenanceTypeRepository;

    @Autowired
    private ScheduledMaintenanceRepository scheduledMaintenanceRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @GetMapping("/types")
    public ResponseEntity<ResponseObject> getMaintenanceTypes() {
        return ResponseEntity.ok(
                new ResponseObject("Maintenances types retrieved successfully", maintenanceTypeRepository.findAll(), TypeResponse.SUCCESS)
        );
    }

    @GetMapping("/scheduled")
    public ResponseEntity<ResponseObject> getScheduledMaintenances() {
        return ResponseEntity.ok(
                new ResponseObject("Scheduled maintenances types retrieved successfully", scheduledMaintenanceRepository.findAll(), TypeResponse.SUCCESS)
        );
    }

    @GetMapping("/init-data")
    public ResponseEntity<ResponseObject> getMaintenanceInitializationData() {
        // Fetch users
        List<MaintenanceInitDataDto.UserSummaryDto> users = userRepository.findAll().stream()
            .map(MaintenanceInitDataDto.UserSummaryDto::new)
            .collect(Collectors.toList());

        // Fetch maintenance types
        List<MaintenanceInitDataDto.MaintenanceTypeSummaryDto> maintenanceTypes = maintenanceTypeRepository.findAll().stream()
            .map(MaintenanceInitDataDto.MaintenanceTypeSummaryDto::new)
            .collect(Collectors.toList());

        // Fetch equipments
        List<MaintenanceInitDataDto.EquipmentSummaryDto> equipments = equipmentRepository.findAll().stream()
            .map(MaintenanceInitDataDto.EquipmentSummaryDto::new)
            .collect(Collectors.toList());

        // Create and return initialization data
        MaintenanceInitDataDto initData = new MaintenanceInitDataDto(
            users, 
            maintenanceTypes, 
            equipments
        );

        ResponseObject responseObject = new ResponseObject(
            "Maintenance initialization data retrieved successfully", 
            initData,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseObject> createMaintenanceRequest(
        @Valid @RequestBody MaintenanceRequestDto requestDto,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Create maintenance request
        Maintenance maintenance = maintenanceService.createMaintenanceRequest(
            requestDto, 
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Maintenance request created successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @PutMapping("/update-status/{maintenanceId}")
    public ResponseEntity<ResponseObject> updateMaintenanceStatus(
        @PathVariable UUID maintenanceId,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update maintenance status
        Maintenance maintenance = maintenanceService.updateMaintenanceStatus(
            maintenanceId,
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Maintenance status updated successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/list")
    public ResponseEntity<ResponseObject> getAllMaintenanceRecords() {
        // Retrieve all maintenance records with full details
        List<MaintenanceDetailDto> maintenanceDetails = maintenanceService.getAllMaintenanceRecords();

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Maintenance records retrieved successfully", 
            maintenanceDetails,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }
} 