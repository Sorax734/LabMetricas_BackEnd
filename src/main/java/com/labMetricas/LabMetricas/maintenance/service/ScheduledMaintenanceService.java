package com.labMetricas.LabMetricas.maintenance.service;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.equipment.repository.EquipmentRepository;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.maintenance.model.ScheduledMaintenance;
import com.labMetricas.LabMetricas.maintenance.model.FrequencyType;
import com.labMetricas.LabMetricas.maintenance.model.dto.ScheduledMaintenanceRequestDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.ScheduledMaintenanceDetailDto;
import com.labMetricas.LabMetricas.maintenance.repository.MaintenanceRepository;
import com.labMetricas.LabMetricas.maintenance.repository.ScheduledMaintenanceRepository;
import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import com.labMetricas.LabMetricas.MaintenanceType.repository.MaintenanceTypeRepository;
import com.labMetricas.LabMetricas.sentEmail.model.SentEmail;
import com.labMetricas.LabMetricas.sentEmail.repository.SentEmailRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScheduledMaintenanceService {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledMaintenanceService.class);

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private ScheduledMaintenanceRepository scheduledMaintenanceRepository;

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
    public Maintenance createScheduledMaintenance(
        ScheduledMaintenanceRequestDto requestDto, 
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
        maintenance.setCode(generateScheduledMaintenanceCode());
        maintenance.setCreatedAt(LocalDateTime.now());
        maintenance.setStatus(true);
        maintenance.setPriority(convertPriority(requestDto.getPriority()));

        // Save maintenance request
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        // Create scheduled maintenance
        ScheduledMaintenance scheduledMaintenance = new ScheduledMaintenance();
        scheduledMaintenance.setMaintenance(savedMaintenance);
        scheduledMaintenance.setNextMaintenance(requestDto.getNextMaintenanceDate());
        scheduledMaintenance.setFrequencyType(convertFrequencyType(requestDto.getFrequencyType()));
        scheduledMaintenance.setFrequencyValue(requestDto.getFrequencyValue().shortValue());

        // Save scheduled maintenance
        scheduledMaintenanceRepository.save(scheduledMaintenance);

        // Create audit log
        createScheduledMaintenanceAuditLog(savedMaintenance, currentUser);

        // Send notification
        sendScheduledMaintenanceNotification(savedMaintenance, responsible);

        return savedMaintenance;
    }

    public String generateScheduledMaintenanceCode() {
        // Generate a unique scheduled maintenance code
        return "SCHED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public FrequencyType convertFrequencyType(ScheduledMaintenanceRequestDto.FrequencyType dtoFrequencyType) {
        return FrequencyType.valueOf(dtoFrequencyType.name());
    }

    private LocalDateTime calculateNextMaintenanceDate(LocalDateTime currentDate, FrequencyType frequencyType, Integer frequencyValue) {
        switch (frequencyType) {
            case DAILY:
                return currentDate.plusDays(frequencyValue);
            case WEEKLY:
                return currentDate.plusWeeks(frequencyValue);
            case MONTHLY:
                return currentDate.plusMonths(frequencyValue);
            case YEARLY:
                return currentDate.plusYears(frequencyValue);
            default:
                return currentDate.plusMonths(1);
        }
    }

    public Maintenance.Priority convertPriority(ScheduledMaintenanceRequestDto.Priority dtoPriority) {
        return Maintenance.Priority.valueOf(dtoPriority.name());
    }

    public void createScheduledMaintenanceAuditLog(Maintenance maintenance, User user) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("CREATE_SCHED_MAINT");
        auditLog.setUser(user);
        auditLog.setCreatedAt(LocalDateTime.now());
        
        auditLogRepository.save(auditLog);
    }

    public void sendScheduledMaintenanceNotification(Maintenance maintenance, User responsible) {
        // Create a sent email record for the scheduled maintenance notification
        SentEmail sentEmail = new SentEmail();
        sentEmail.setSubject("New Scheduled Maintenance: " + maintenance.getCode());
        sentEmail.setBody(createScheduledMaintenanceNotificationBody(maintenance));
        sentEmail.setUser(responsible);
        sentEmail.setCreatedAt(LocalDateTime.now());

        sentEmailRepository.save(sentEmail);
    }

    private String createScheduledMaintenanceNotificationBody(Maintenance maintenance) {
        return String.format(
            "Dear %s,\n\n" +
            "A new scheduled maintenance has been created for the following equipment:\n\n" +
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
    public Maintenance updateScheduledMaintenanceStatus(UUID maintenanceId, User currentUser) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Scheduled Maintenance not found"));
        
        // Toggle the status
        maintenance.setStatus(!maintenance.getStatus());
        maintenance.setUpdatedAt(LocalDateTime.now());

        Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log for status update
        createScheduledMaintenanceAuditLog(updatedMaintenance, currentUser);

        // Send notification about status update
        sendScheduledMaintenanceStatusUpdateNotification(updatedMaintenance, currentUser);

        return updatedMaintenance;
    }

    private void sendScheduledMaintenanceStatusUpdateNotification(Maintenance maintenance, User responsible) {
        // Create a sent email record for the scheduled maintenance status update
        SentEmail sentEmail = new SentEmail();
        sentEmail.setSubject("Scheduled Maintenance Status Update: " + maintenance.getCode());
        sentEmail.setBody(createScheduledMaintenanceStatusUpdateBody(maintenance));
        sentEmail.setUser(responsible);
        sentEmail.setCreatedAt(LocalDateTime.now());

        sentEmailRepository.save(sentEmail);
    }

    private String createScheduledMaintenanceStatusUpdateBody(Maintenance maintenance) {
        return String.format(
            "Dear %s,\n\n" +
            "The status of scheduled maintenance %s has been updated.\n\n" +
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

    @Transactional(readOnly = true)
    public List<ScheduledMaintenanceDetailDto> getAllScheduledMaintenanceRecords() {
        return maintenanceRepository.findAll().stream()
            .filter(maintenance -> maintenance.getScheduledMaintenance() != null)
            .map(maintenance -> new ScheduledMaintenanceDetailDto(maintenance, maintenance.getScheduledMaintenance()))
            .collect(Collectors.toList());
    }

    @Transactional
    public Maintenance updateScheduledMaintenanceRequest(
        UUID maintenanceId, 
        ScheduledMaintenanceRequestDto requestDto, 
        User currentUser
    ) {
        // Find existing maintenance
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Scheduled Maintenance not found"));
        
        // Validate and fetch related entities
        Equipment equipment = equipmentRepository.findById(requestDto.getEquipmentId())
            .orElseThrow(() -> new EntityNotFoundException("Equipment not found"));
        
        MaintenanceType maintenanceType = maintenanceTypeRepository.findById(requestDto.getMaintenanceTypeId())
            .orElseThrow(() -> new EntityNotFoundException("Maintenance Type not found"));
        
        User responsible = userRepository.findByIdWithRole(requestDto.getResponsibleUserId())
            .orElseThrow(() -> new EntityNotFoundException("Responsible User not found"));

        // Update maintenance details
        maintenance.setDescription(requestDto.getDescription());
        maintenance.setEquipment(equipment);
        maintenance.setMaintenanceType(maintenanceType);
        maintenance.setResponsible(responsible);
        maintenance.setUpdatedAt(LocalDateTime.now());
        maintenance.setPriority(convertPriority(requestDto.getPriority()));

        // Save updated maintenance
        Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

        // Update scheduled maintenance details
        ScheduledMaintenance scheduledMaintenance = maintenance.getScheduledMaintenance();
        if (scheduledMaintenance != null) {
            scheduledMaintenance.setNextMaintenance(requestDto.getNextMaintenanceDate());
            scheduledMaintenance.setFrequencyType(convertFrequencyType(requestDto.getFrequencyType()));
            scheduledMaintenance.setFrequencyValue(requestDto.getFrequencyValue().shortValue());
            scheduledMaintenanceRepository.save(scheduledMaintenance);
        }

        // Create audit log
        createScheduledMaintenanceAuditLog(updatedMaintenance, currentUser);

        return updatedMaintenance;
    }

    @Transactional
    public Maintenance logicalDeleteScheduledMaintenance(
        UUID maintenanceId, 
        User currentUser
    ) {
        // Find existing maintenance
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Scheduled Maintenance not found"));
        
        // Set deletion timestamp for logical deletion
        maintenance.setDeletedAt(LocalDateTime.now());
        maintenance.setStatus(!maintenance.getStatus());
        maintenance.setUpdatedAt(LocalDateTime.now());

        // Save updated maintenance
        Maintenance deletedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log
        createScheduledMaintenanceAuditLog(deletedMaintenance, currentUser);

        return deletedMaintenance;
    }

    @Transactional(readOnly = true)
    public ScheduledMaintenanceDetailDto getScheduledMaintenanceById(UUID maintenanceId) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Scheduled Maintenance not found"));
        
        if (maintenance.getScheduledMaintenance() == null) {
            throw new EntityNotFoundException("This maintenance is not a scheduled maintenance");
        }
        
        return new ScheduledMaintenanceDetailDto(maintenance, maintenance.getScheduledMaintenance());
    }
} 