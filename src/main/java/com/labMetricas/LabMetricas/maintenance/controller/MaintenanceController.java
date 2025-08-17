package com.labMetricas.LabMetricas.maintenance.controller;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.equipment.repository.EquipmentRepository;
import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceInitDataDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceRequestDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceDetailDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceSubmitForReviewDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceRejectionDto;

import com.labMetricas.LabMetricas.MaintenanceType.repository.MaintenanceTypeRepository;
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

    @PostMapping
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

    @PutMapping("/{maintenanceId}")
    public ResponseEntity<ResponseObject> updateMaintenanceRequest(
        @PathVariable UUID maintenanceId,
        @Valid @RequestBody MaintenanceRequestDto requestDto,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Update maintenance request
        Maintenance maintenance = maintenanceService.updateMaintenanceRequest(
            maintenanceId, 
            requestDto, 
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Maintenance request updated successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @DeleteMapping("/{maintenanceId}")
    public ResponseEntity<ResponseObject> logicalDeleteMaintenance(
        @PathVariable UUID maintenanceId,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Perform logical deletion
        Maintenance maintenance = maintenanceService.logicalDeleteMaintenance(
            maintenanceId,
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Maintenance request deleted successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/{maintenanceId}")
    public ResponseEntity<ResponseObject> getMaintenanceById(
        @PathVariable UUID maintenanceId
    ) {
        // Retrieve maintenance details
        MaintenanceDetailDto maintenanceDetail = maintenanceService.getMaintenanceById(maintenanceId);

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Maintenance record retrieved successfully", 
            maintenanceDetail,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping
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

    @PostMapping("/submit-for-review")
    public ResponseEntity<ResponseObject> submitMaintenanceForReview(
        @Valid @RequestBody MaintenanceSubmitForReviewDto requestDto,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Submit maintenance for review
        Maintenance maintenance = maintenanceService.submitMaintenanceForReview(
            requestDto.getMaintenanceId(),
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Maintenance submitted for review successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }







    @GetMapping("/status/{reviewStatus}")
    public ResponseEntity<ResponseObject> getMaintenanceByReviewStatus(
        @PathVariable String reviewStatus
    ) {
        try {
            Maintenance.ReviewStatus status = Maintenance.ReviewStatus.valueOf(reviewStatus.toUpperCase());
            List<MaintenanceDetailDto> maintenanceList = maintenanceService.getMaintenanceByReviewStatus(status);

            ResponseObject responseObject = new ResponseObject(
                "Maintenance records retrieved successfully", 
                maintenanceList,
                TypeResponse.SUCCESS
            );

            return ResponseEntity.ok(responseObject);
        } catch (IllegalArgumentException e) {
            ResponseObject responseObject = new ResponseObject(
                "Invalid review status: " + reviewStatus, 
                null,
                TypeResponse.ERROR
            );
            return ResponseEntity.badRequest().body(responseObject);
        }
    }

    @GetMapping("/responsible/{userId}")
    public ResponseEntity<ResponseObject> getMaintenanceByResponsibleUser(
        @PathVariable UUID userId
    ) {
        List<MaintenanceDetailDto> maintenanceList = maintenanceService.getMaintenanceByResponsibleUser(userId);

        ResponseObject responseObject = new ResponseObject(
            "Maintenance records for responsible user retrieved successfully", 
            maintenanceList,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/requested/{userId}")
    public ResponseEntity<ResponseObject> getMaintenanceByRequestedUser(
        @PathVariable UUID userId
    ) {
        List<MaintenanceDetailDto> maintenanceList = maintenanceService.getMaintenanceByRequestedUser(userId);

        ResponseObject responseObject = new ResponseObject(
            "Maintenance records for requested user retrieved successfully", 
            maintenanceList,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/pending-review")
    public ResponseEntity<ResponseObject> getPendingReviewMaintenance() {
        List<MaintenanceDetailDto> maintenanceList = maintenanceService.getPendingReviewMaintenance();

        ResponseObject responseObject = new ResponseObject(
            "Pending review maintenance records retrieved successfully", 
            maintenanceList,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/approved")
    public ResponseEntity<ResponseObject> getApprovedMaintenance() {
        List<MaintenanceDetailDto> maintenanceList = maintenanceService.getApprovedMaintenance();

        ResponseObject responseObject = new ResponseObject(
            "Approved maintenance records retrieved successfully", 
            maintenanceList,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/in-progress")
    public ResponseEntity<ResponseObject> getInProgressMaintenance() {
        List<MaintenanceDetailDto> maintenanceList = maintenanceService.getInProgressMaintenance();

        ResponseObject responseObject = new ResponseObject(
            "In progress maintenance records retrieved successfully", 
            maintenanceList,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/rejected")
    public ResponseEntity<ResponseObject> getRejectedMaintenance() {
        List<MaintenanceDetailDto> maintenanceList = maintenanceService.getRejectedMaintenance();

        ResponseObject responseObject = new ResponseObject(
            "Rejected maintenance records retrieved successfully", 
            maintenanceList,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/created-by-me")
    public ResponseEntity<ResponseObject> getMaintenanceCreatedByMe(Authentication authentication) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<MaintenanceDetailDto> maintenanceList = maintenanceService.getMaintenanceCreatedByUser(currentUser);

        ResponseObject responseObject = new ResponseObject(
            "Maintenance records created by you retrieved successfully", 
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

        List<MaintenanceDetailDto> maintenanceList = maintenanceService.getMaintenanceAssignedToUser(currentUser);

        ResponseObject responseObject = new ResponseObject(
            "Maintenance records assigned to you retrieved successfully", 
            maintenanceList,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @GetMapping("/my-maintenance")
    public ResponseEntity<ResponseObject> getMyMaintenance(Authentication authentication) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<MaintenanceDetailDto> maintenanceList = maintenanceService.getMyMaintenance(currentUser);

        ResponseObject responseObject = new ResponseObject(
            "All your maintenance records (created and assigned) retrieved successfully", 
            maintenanceList,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @PostMapping("/approved/{maintenanceId}")
    public ResponseEntity<ResponseObject> approveMaintenance(
        @PathVariable UUID maintenanceId,
        Authentication authentication
    ) {
        // Get current user from authentication
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Approve maintenance
        Maintenance maintenance = maintenanceService.approveMaintenance(
            maintenanceId,
            currentUser
        );

        // Prepare response
        ResponseObject responseObject = new ResponseObject(
            "Maintenance approved successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }

    @PostMapping("/rejected/{maintenanceId}")
    public ResponseEntity<ResponseObject> rejectMaintenance(
        @PathVariable UUID maintenanceId,
        @Valid @RequestBody MaintenanceRejectionDto rejectionDto,
        Authentication authentication
    ) {
        User currentUser = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found"));
        Maintenance maintenance = maintenanceService.rejectMaintenance(
            maintenanceId,
            rejectionDto.getRejectionReason(),
            currentUser
        );
        ResponseObject responseObject = new ResponseObject(
            "Maintenance rejected successfully", 
            maintenance,
            TypeResponse.SUCCESS
        );

        return ResponseEntity.ok(responseObject);
    }
} 