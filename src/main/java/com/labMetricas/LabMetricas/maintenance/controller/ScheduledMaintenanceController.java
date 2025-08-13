package com.labMetricas.LabMetricas.maintenance.controller;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.equipment.repository.EquipmentRepository;
import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceInitDataDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.ScheduledMaintenanceRequestDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.ScheduledMaintenanceDetailDto;
import com.labMetricas.LabMetricas.MaintenanceType.repository.MaintenanceTypeRepository;
import com.labMetricas.LabMetricas.maintenance.service.ScheduledMaintenanceService;
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
@RequestMapping("/api/scheduled-maintenance")
public class ScheduledMaintenanceController {

    @Autowired
    private ScheduledMaintenanceService scheduledMaintenanceService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MaintenanceTypeRepository maintenanceTypeRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @GetMapping("/init-data")
    public ResponseEntity<ResponseObject> getScheduledMaintenanceInitializationData() {
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
            "Scheduled maintenance initialization data retrieved successfully", 
            initData,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @PostMapping
    public ResponseEntity<ResponseObject> createScheduledMaintenanceRequest(
        @Valid @RequestBody ScheduledMaintenanceRequestDto requestDto,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Log for debugging
        System.out.println("Controller - Current user: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");

        // Create scheduled maintenance request
        Maintenance maintenance = scheduledMaintenanceService.createScheduledMaintenance(
            requestDto, 
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Scheduled maintenance request created successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @PutMapping("/update-status/{maintenanceId}")
    public ResponseEntity<ResponseObject> updateScheduledMaintenanceStatus(
        @PathVariable UUID maintenanceId,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update scheduled maintenance status
        Maintenance maintenance = scheduledMaintenanceService.updateScheduledMaintenanceStatus(
            maintenanceId,
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Scheduled maintenance status updated successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @PutMapping("/{maintenanceId}")
    public ResponseEntity<ResponseObject> updateScheduledMaintenanceRequest(
        @PathVariable UUID maintenanceId,
        @Valid @RequestBody ScheduledMaintenanceRequestDto requestDto,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update scheduled maintenance request
        Maintenance maintenance = scheduledMaintenanceService.updateScheduledMaintenanceRequest(
            maintenanceId, 
            requestDto, 
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Scheduled maintenance request updated successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @DeleteMapping("/{maintenanceId}")
    public ResponseEntity<ResponseObject> logicalDeleteScheduledMaintenance(
        @PathVariable UUID maintenanceId,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Perform logical deletion
        Maintenance maintenance = scheduledMaintenanceService.logicalDeleteScheduledMaintenance(
            maintenanceId,
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Scheduled maintenance request deleted successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/{maintenanceId}")
    public ResponseEntity<ResponseObject> getScheduledMaintenanceById(
        @PathVariable UUID maintenanceId
    ) {
        // Retrieve scheduled maintenance details
        ScheduledMaintenanceDetailDto scheduledMaintenanceDetail = scheduledMaintenanceService.getScheduledMaintenanceById(maintenanceId);

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Scheduled maintenance record retrieved successfully", 
            scheduledMaintenanceDetail,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping
    public ResponseEntity<ResponseObject> getAllScheduledMaintenanceRecords() {
        // Retrieve all scheduled maintenance records with full details
        List<ScheduledMaintenanceDetailDto> scheduledMaintenanceDetails = scheduledMaintenanceService.getAllScheduledMaintenanceRecords();

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Scheduled maintenance records retrieved successfully", 
            scheduledMaintenanceDetails,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/created-by-me")
    public ResponseEntity<ResponseObject> getMaintenanceCreatedByMe(Authentication authentication) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Log for debugging
        System.out.println("Controller - Searching for maintenance created by: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");

        List<ScheduledMaintenanceDetailDto> maintenanceList = scheduledMaintenanceService.getMaintenanceCreatedByUser(currentUser);

        ResponseObject responseObject = new ResponseObject(
                "Scheduled maintenance records created by you retrieved successfully",
                maintenanceList,
                TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/assigned-to-me")
    public ResponseEntity<ResponseObject> getMaintenanceAssignedToMe(Authentication authentication) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ScheduledMaintenanceDetailDto> maintenanceList = scheduledMaintenanceService.getMaintenanceAssignedToUser(currentUser);

        ResponseObject responseObject = new ResponseObject(
                "Scheduled maintenance records assigned to you retrieved successfully",
                maintenanceList,
                TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }
} 