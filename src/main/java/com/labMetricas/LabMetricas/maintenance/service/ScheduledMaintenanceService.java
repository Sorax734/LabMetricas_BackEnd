package com.labMetricas.LabMetricas.maintenance.service;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
import com.labMetricas.LabMetricas.config.ProductionEmailService;
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

    @Autowired
    private ProductionEmailService productionEmailService;

    @Transactional
    public Maintenance createScheduledMaintenance(
        ScheduledMaintenanceRequestDto requestDto, 
        User currentUser
    ) {
        // Log current user information
        logger.info("Creating scheduled maintenance with currentUser: {} (ID: {})", 
            currentUser != null ? currentUser.getEmail() : "NULL", 
            currentUser != null ? currentUser.getId() : "NULL");
        
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
        maintenance.setRequestedBy(currentUser); // Set the creator
        
        logger.info("Set requestedBy to: {} (ID: {})", 
            maintenance.getRequestedBy() != null ? maintenance.getRequestedBy().getEmail() : "NULL",
            maintenance.getRequestedBy() != null ? maintenance.getRequestedBy().getId() : "NULL");
        
        // Generate the new format code
        String generatedCode = generateMaintenanceCode(maintenanceType, true);
        logger.info("Generated maintenance code: {}", generatedCode);
        
        maintenance.setCode(generatedCode); // Programmed maintenance
        maintenance.setCreatedAt(LocalDateTime.now());
        maintenance.setStatus(true);
        maintenance.setPriority(convertPriority(requestDto.getPriority()));

        // Save maintenance request
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);
        
        logger.info("Saved maintenance with code: {} and requestedBy: {}", 
            savedMaintenance.getCode(), 
            savedMaintenance.getRequestedBy() != null ? savedMaintenance.getRequestedBy().getEmail() : "NULL");

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

    public String generateMaintenanceCode(MaintenanceType maintenanceType, boolean isProgrammed) {
        // Generate a unique maintenance code with the format: YYYY-MM-DD-TYPE-P/NP-COUNTER
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Get maintenance type initials (first 3 characters)
        String typeInitials = getMaintenanceTypeInitials(maintenanceType);
        
        // Determine if it's programmed (P) or non-programmed (NP)
        String programType = isProgrammed ? "P" : "NP";
        
        // Get counter for the specific type
        String counter = getNextCounter(isProgrammed);
        
        String finalCode = date + "-" + typeInitials + "-" + programType + "-" + counter;
        logger.info("Generated code: date={}, typeInitials={}, programType={}, counter={}, finalCode={}", 
            date, typeInitials, programType, counter, finalCode);
        
        return finalCode;
    }
    
    public String getMaintenanceTypeInitials(MaintenanceType maintenanceType) {
        // Get the first 3 characters of the maintenance type name
        String name = maintenanceType.getName();
        if (name.length() >= 3) {
            return name.substring(0, 3).toUpperCase();
        } else {
            return name.toUpperCase();
        }
    }
    
    public String getNextCounter(boolean isProgrammed) {
        // Get the next counter for the specific program type
        long count;
        if (isProgrammed) {
            count = maintenanceRepository.countByScheduledMaintenanceIsNotNull();
            logger.info("Count for programmed maintenance: {}", count);
        } else {
            count = maintenanceRepository.countByScheduledMaintenanceIsNull();
            logger.info("Count for non-programmed maintenance: {}", count);
        }
        String counter = String.format("%04d", count + 1);
        logger.info("Generated counter: {}", counter);
        return counter;
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
        // Send real email notification using ProductionEmailService
        try {
            String maintenanceType = maintenance.getMaintenanceType().getName();
            String scheduledDate = maintenance.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            
            productionEmailService.sendMaintenanceNotification(
                responsible.getEmail(),
                responsible.getName(),
                maintenanceType,
                scheduledDate
            );
            
            logger.info("Scheduled maintenance notification email sent to: {}", responsible.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send scheduled maintenance notification email to: {}", responsible.getEmail(), e);
        }
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
        // Send real email notification using ProductionEmailService
        try {
            String action = "Actualización de Estado de Mantenimiento Programado";
            String details = String.format(
                "El estado del mantenimiento programado %s para el equipo %s ha sido actualizado a: %s",
                maintenance.getCode(),
                maintenance.getEquipment().getName(),
                maintenance.getStatus() ? "Activo" : "Inactivo"
            );
            
            productionEmailService.sendActionConfirmation(
                responsible.getEmail(),
                responsible.getName(),
                action,
                details
            );
            
            logger.info("Scheduled maintenance status update email sent to: {}", responsible.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send scheduled maintenance status update email to: {}", responsible.getEmail(), e);
        }
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

        // Send notification about maintenance update
        sendScheduledMaintenanceUpdateNotification(updatedMaintenance, currentUser);

        return updatedMaintenance;
    }

    private void sendScheduledMaintenanceUpdateNotification(Maintenance maintenance, User currentUser) {
        // Send real email notification using ProductionEmailService
        try {
            String action = "Actualización de Mantenimiento Programado";
            String details = String.format(
                "El mantenimiento programado %s para el equipo %s ha sido actualizado por %s",
                maintenance.getCode(),
                maintenance.getEquipment().getName(),
                currentUser.getName()
            );
            
            productionEmailService.sendActionConfirmation(
                maintenance.getResponsible().getEmail(),
                maintenance.getResponsible().getName(),
                action,
                details
            );
            
            logger.info("Scheduled maintenance update email sent to: {}", maintenance.getResponsible().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send scheduled maintenance update email to: {}", maintenance.getResponsible().getEmail(), e);
        }
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

        // Send notification about maintenance deletion
        sendScheduledMaintenanceDeletionNotification(deletedMaintenance, currentUser);

        return deletedMaintenance;
    }

    private void sendScheduledMaintenanceDeletionNotification(Maintenance maintenance, User currentUser) {
        // Send real email notification using ProductionEmailService
        try {
            String action = "Eliminación de Mantenimiento Programado";
            String details = String.format(
                "El mantenimiento programado %s para el equipo %s ha sido marcado como eliminado por %s",
                maintenance.getCode(),
                maintenance.getEquipment().getName(),
                currentUser.getName()
            );
            
            productionEmailService.sendActionConfirmation(
                maintenance.getResponsible().getEmail(),
                maintenance.getResponsible().getName(),
                action,
                details
            );
            
            logger.info("Scheduled maintenance deletion email sent to: {}", maintenance.getResponsible().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send scheduled maintenance deletion email to: {}", maintenance.getResponsible().getEmail(), e);
        }
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

    @Transactional(readOnly = true)
    public List<ScheduledMaintenanceDetailDto> getMaintenanceCreatedByUser(User user) {
        logger.info("Searching for maintenance created by user: {} (ID: {})", user.getEmail(), user.getId());
        
        List<Maintenance> maintenanceList = maintenanceRepository.findByRequestedByAndScheduledMaintenanceIsNotNullOrderByCreatedAtDesc(user);
        logger.info("Found {} maintenance records for user", maintenanceList.size());
        
        // Log each maintenance found for debugging
        for (Maintenance maintenance : maintenanceList) {
            logger.info("Maintenance ID: {}, Code: {}, RequestedBy: {}", 
                maintenance.getId(), 
                maintenance.getCode(),
                maintenance.getRequestedBy() != null ? maintenance.getRequestedBy().getEmail() : "NULL");
        }
        
        return maintenanceList.stream()
                .filter(maintenance -> maintenance.getScheduledMaintenance() != null)
                .map(maintenance -> new ScheduledMaintenanceDetailDto(maintenance, maintenance.getScheduledMaintenance()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScheduledMaintenanceDetailDto> getMaintenanceAssignedToUser(User user) {
        return maintenanceRepository.findByResponsibleAndScheduledMaintenanceIsNotNullOrderByCreatedAtDesc(user).stream()
                .filter(maintenance -> maintenance.getScheduledMaintenance() != null)
                .map(maintenance -> new ScheduledMaintenanceDetailDto(maintenance, maintenance.getScheduledMaintenance()))
                .collect(Collectors.toList());
    }
} 