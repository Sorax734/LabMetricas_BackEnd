package com.labMetricas.LabMetricas.maintenance.service;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.equipment.repository.EquipmentRepository;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.maintenance.model.MaintenanceType;
import com.labMetricas.LabMetricas.maintenance.repository.MaintenanceRepository;
import com.labMetricas.LabMetricas.maintenance.repository.MaintenanceTypeRepository;
import com.labMetricas.LabMetricas.sentEmail.model.SentEmail;
import com.labMetricas.LabMetricas.sentEmail.repository.SentEmailRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceRequestDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceDetailDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MaintenanceService {

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private MaintenanceTypeRepository maintenanceTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private SentEmailRepository sentEmailRepository;

    @Transactional
    public Maintenance createMaintenanceRequest(
        MaintenanceRequestDto requestDto, 
        User currentUser
    ) {
        // Validate and fetch related entities
        Equipment equipment = equipmentRepository.findById(requestDto.getEquipmentId())
            .orElseThrow(() -> new EntityNotFoundException("Equipment not found"));
        
        MaintenanceType maintenanceType = maintenanceTypeRepository.findById(requestDto.getMaintenanceTypeId())
            .orElseThrow(() -> new EntityNotFoundException("Maintenance Type not found"));
        
        User responsible = userRepository.findByIdWithRole(requestDto.getResponsibleUserId())
            .orElseThrow(() -> new EntityNotFoundException("Responsible User not found"));

        // Create maintenance request
        Maintenance maintenance = new Maintenance();
        maintenance.setDescription(requestDto.getDescription());
        maintenance.setEquipment(equipment);
        maintenance.setMaintenanceType(maintenanceType);
        maintenance.setResponsible(responsible);
        maintenance.setCode(generateMaintenanceCode());
        maintenance.setCreatedAt(LocalDateTime.now());
        maintenance.setStatus(true);
        maintenance.setPriority(convertPriority(requestDto.getPriority()));

        // Save maintenance request
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log
        createMaintenanceAuditLog(savedMaintenance, currentUser);

        // Send notification
        sendMaintenanceNotification(savedMaintenance, responsible);

        return savedMaintenance;
    }

    private String generateMaintenanceCode() {
        // Generate a unique maintenance code
        return "MAINT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void createMaintenanceAuditLog(Maintenance maintenance, User user) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("CREATE_MAINTENANCE");
        auditLog.setUser(user);
        auditLog.setCreatedAt(LocalDateTime.now());
        
        auditLogRepository.save(auditLog);
    }

    private void sendMaintenanceNotification(Maintenance maintenance, User responsible) {
        // Create a sent email record for the maintenance notification
        SentEmail sentEmail = new SentEmail();
        sentEmail.setSubject("New Maintenance Request: " + maintenance.getCode());
        sentEmail.setBody(createMaintenanceNotificationBody(maintenance));
        sentEmail.setUser(responsible);
        sentEmail.setCreatedAt(LocalDateTime.now());

        sentEmailRepository.save(sentEmail);
    }

    private String createMaintenanceNotificationBody(Maintenance maintenance) {
        return String.format(
            "Dear %s,\n\n" +
            "A new maintenance request has been created for the following equipment:\n\n" +
            "Maintenance Code: %s\n" +
            "Equipment: %s (Code: %s)\n" +
            "Description: %s\n" +
            "Maintenance Type: %s\n\n" +
            "Please review and take necessary actions.\n\n" +
            "Best regards,\n" +
            "Maintenance Management System",
            maintenance.getResponsible().getName(),
            maintenance.getCode(),
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode(),
            maintenance.getDescription(),
            maintenance.getMaintenanceType().getName()
        );
    }   

    @Transactional
    public Maintenance updateMaintenanceStatus(UUID maintenanceId, User currentUser) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));
        
        maintenance.setStatus(!maintenance.getStatus());
        maintenance.setUpdatedAt(LocalDateTime.now());

        Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log for status update
        createMaintenanceAuditLog(updatedMaintenance, currentUser);

        // Send notification about status update
        sendMaintenanceStatusUpdateNotification(updatedMaintenance, currentUser);

        return updatedMaintenance;
    }

    private void sendMaintenanceStatusUpdateNotification(Maintenance maintenance, User responsible) {
        // Create a sent email record for the maintenance status update
        SentEmail sentEmail = new SentEmail();
        sentEmail.setSubject("Maintenance Request Status Update: " + maintenance.getCode());
        sentEmail.setBody(createMaintenanceStatusUpdateBody(maintenance));
        sentEmail.setUser(responsible);
        sentEmail.setCreatedAt(LocalDateTime.now());

        sentEmailRepository.save(sentEmail);
    }

    private String createMaintenanceStatusUpdateBody(Maintenance maintenance) {
        return String.format(
            "Dear %s,\n\n" +
            "The status of maintenance request %s has been updated.\n\n" +
            "Equipment: %s (Code: %s)\n" +
            "Current Status: %s\n\n" +
            "Please review the changes.\n\n" +
            "Best regards,\n" +
            "Maintenance Management System",
            maintenance.getResponsible().getName(),
            maintenance.getCode(),
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode(),
            maintenance.getStatus() ? "Active" : "Inactive"
        );
    }

    private Maintenance.Priority convertPriority(MaintenanceRequestDto.Priority dtoPriority) {
        return Maintenance.Priority.valueOf(dtoPriority.name());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getAllMaintenanceRecords() {
        return maintenanceRepository.findAll().stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }
} 