package com.labMetricas.LabMetricas.maintenance.service;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
import com.labMetricas.LabMetricas.config.ProductionEmailService;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.equipment.repository.EquipmentRepository;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import com.labMetricas.LabMetricas.maintenance.repository.MaintenanceRepository;
import com.labMetricas.LabMetricas.MaintenanceType.repository.MaintenanceTypeRepository;
import com.labMetricas.LabMetricas.Notice.service.NoticeService;
import com.labMetricas.LabMetricas.sentEmail.model.SentEmail;
import com.labMetricas.LabMetricas.sentEmail.repository.SentEmailRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceRequestDto;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceDetailDto;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Service
public class MaintenanceService {
    private static final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);

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

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private ProductionEmailService productionEmailService;

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
        maintenance.setRequestedBy(currentUser); // Set the creator
        maintenance.setCode(generateMaintenanceCode(maintenanceType, false)); // Non-programmed maintenance
        maintenance.setCreatedAt(LocalDateTime.now());
        maintenance.setStatus(true);
        maintenance.setPriority(convertPriority(requestDto.getPriority()));
        maintenance.setReviewStatus(Maintenance.ReviewStatus.IN_PROGRESS); // Automatically in progress

        // Save maintenance request
        Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log
        createMaintenanceAuditLog(savedMaintenance, currentUser);

        // Send notification to responsible person about new assignment
        sendMaintenanceAssignmentNotification(savedMaintenance, responsible, currentUser);

        // Create notice notification for the responsible person
        noticeService.createMaintenanceNotice(savedMaintenance, responsible);

        return savedMaintenance;
    }

    private String generateMaintenanceCode(MaintenanceType maintenanceType, boolean isProgrammed) {
        // Generate a unique maintenance code with the format: YYYY-MM-DD-TYPE-P/NP-COUNTER
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Get maintenance type initials (first 3 characters)
        String typeInitials = getMaintenanceTypeInitials(maintenanceType);
        
        // Determine if it's programmed (P) or non-programmed (NP)
        String programType = isProgrammed ? "P" : "NP";
        
        // Get counter for the specific type
        String counter = getNextCounter(isProgrammed);
        
        return date + "-" + typeInitials + "-" + programType + "-" + counter;
    }
    
    private String getMaintenanceTypeInitials(MaintenanceType maintenanceType) {
        // Get the first 3 characters of the maintenance type name
        String name = maintenanceType.getName();
        if (name.length() >= 3) {
            return name.substring(0, 3).toUpperCase();
        } else {
            return name.toUpperCase();
        }
    }
    
    private String getNextCounter(boolean isProgrammed) {
        // Get the next counter for the specific program type
        long count;
        if (isProgrammed) {
            count = maintenanceRepository.countByScheduledMaintenanceIsNotNull();
        } else {
            count = maintenanceRepository.countByScheduledMaintenanceIsNull();
        }
        return String.format("%04d", count + 1);
    }

    private void createMaintenanceAuditLog(Maintenance maintenance, User user) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("CREATE_MAINTENANCE");
        auditLog.setUser(user);
        auditLog.setCreatedAt(LocalDateTime.now());
        
        auditLogRepository.save(auditLog);
    }

    private void sendMaintenanceNotification(Maintenance maintenance, User responsible) {
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
            
            logger.info("Maintenance notification email sent to: {}", responsible.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send maintenance notification email to: {}", responsible.getEmail(), e);
        }
    }

    @Transactional
    public Maintenance updateMaintenanceStatus(UUID maintenanceId, User currentUser) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));
        
        // Toggle the status
        maintenance.setStatus(!maintenance.getStatus());
        maintenance.setUpdatedAt(LocalDateTime.now());

        Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log for status update
        createMaintenanceAuditLog(updatedMaintenance, currentUser);

        // Send notification about status update
        sendMaintenanceStatusUpdateNotification(updatedMaintenance, currentUser);

        // If maintenance is completed (status = false), delete related notices
        if (!updatedMaintenance.getStatus()) {
            noticeService.deleteNoticesByMaintenanceCode(updatedMaintenance.getCode());
        }

        return updatedMaintenance;
    }

    private void sendMaintenanceStatusUpdateNotification(Maintenance maintenance, User responsible) {
        // Send real email notification using ProductionEmailService
        try {
            String action = "Actualización de Estado de Mantenimiento";
            String details = String.format(
                "El mantenimiento %s para el equipo %s ha cambiado de estado a: %s",
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
            
            logger.info("Maintenance status update email sent to: {}", responsible.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send maintenance status update email to: {}", responsible.getEmail(), e);
        }
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

    @Transactional
    public Maintenance updateMaintenanceRequest(
        UUID maintenanceId, 
        MaintenanceRequestDto requestDto, 
        User currentUser
    ) {
        // Find existing maintenance
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));
        
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
        // Explicitly do NOT modify deletedAt

        // Save updated maintenance
        Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log
        createMaintenanceAuditLog(updatedMaintenance, currentUser);

        // Send notification about maintenance update
        sendMaintenanceUpdateNotification(updatedMaintenance, currentUser);

        return updatedMaintenance;
    }

    private void sendMaintenanceUpdateNotification(Maintenance maintenance, User currentUser) {
        // Send real email notification using ProductionEmailService
        try {
            String action = "Actualización de Mantenimiento";
            String details = String.format(
                "El mantenimiento %s para el equipo %s ha sido actualizado por %s",
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
            
            logger.info("Maintenance update email sent to: {}", maintenance.getResponsible().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send maintenance update email to: {}", maintenance.getResponsible().getEmail(), e);
        }
    }

    @Transactional
    public Maintenance logicalDeleteMaintenance(
        UUID maintenanceId, 
        User currentUser
    ) {
        // Find existing maintenance
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));
        
        // Set deletion timestamp for logical deletion
        maintenance.setDeletedAt(LocalDateTime.now());
        maintenance.setStatus(!maintenance.getStatus());
        maintenance.setUpdatedAt(LocalDateTime.now());

        // Save updated maintenance
        Maintenance deletedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log
        createMaintenanceAuditLog(deletedMaintenance, currentUser);

        // Send notification about maintenance deletion
        sendMaintenanceDeletionNotification(deletedMaintenance, currentUser);

        return deletedMaintenance;
    }

    private void sendMaintenanceDeletionNotification(Maintenance maintenance, User currentUser) {
        // Send real email notification using ProductionEmailService
        try {
            String action = "Eliminación de Mantenimiento";
            String details = String.format(
                "El mantenimiento %s para el equipo %s ha sido marcado como eliminado por %s",
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
            
            logger.info("Maintenance deletion email sent to: {}", maintenance.getResponsible().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send maintenance deletion email to: {}", maintenance.getResponsible().getEmail(), e);
        }
    }

    @Transactional(readOnly = true)
    public MaintenanceDetailDto getMaintenanceById(UUID maintenanceId) {
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));
        
        return new MaintenanceDetailDto(maintenance);
    }

    @Transactional
    public Maintenance submitMaintenanceForReview(
        UUID maintenanceId, 
        User currentUser
    ) {
        // Find existing maintenance
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));
        
        // Validate that the current user is the responsible person
        if (!maintenance.getResponsible().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the responsible person can submit maintenance for review");
        }
        
        // Validate that maintenance is in progress
        if (maintenance.getReviewStatus() != Maintenance.ReviewStatus.IN_PROGRESS) {
            throw new RuntimeException("Maintenance must be in progress to submit for review");
        }
        
        // Update maintenance status to pending review
        maintenance.setReviewStatus(Maintenance.ReviewStatus.PENDING);
        maintenance.setUpdatedAt(LocalDateTime.now());

        Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log
        createMaintenanceAuditLog(updatedMaintenance, currentUser);

        // Send notification to creator (who assigned the maintenance)
        sendMaintenanceReviewRequestNotification(updatedMaintenance, maintenance.getRequestedBy(), currentUser);

        // Create notice notification for the creator
        noticeService.createMaintenanceReviewRequestNotice(updatedMaintenance, maintenance.getRequestedBy());

        return updatedMaintenance;
    }

    private void sendMaintenanceReviewRequestNotification(Maintenance maintenance, User reviewer, User creator) {
        // Send real email notification using ProductionEmailService
        try {
            String action = "Solicitud de Revisión de Mantenimiento";
            String details = String.format(
                "El mantenimiento %s para el equipo %s ha sido enviado para revisión por %s. " +
                "Por favor, revisa y aprueba o rechaza este mantenimiento.",
                maintenance.getCode(),
                maintenance.getEquipment().getName(),
                creator.getName()
            );
            
            productionEmailService.sendActionConfirmation(
                reviewer.getEmail(),
                reviewer.getName(),
                action,
                details
            );
            
            logger.info("Maintenance review request email sent to: {}", reviewer.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send maintenance review request email to: {}", reviewer.getEmail(), e);
        }
    }

    private void sendMaintenanceAssignmentNotification(Maintenance maintenance, User responsible, User creator) {
        // Send real email notification using ProductionEmailService
        try {
            String action = "Nueva Asignación de Mantenimiento";
            String details = String.format(
                "Se te ha asignado un nuevo mantenimiento: %s para el equipo %s. " +
                "Tipo: %s, Prioridad: %s, Descripción: %s. " +
                "Asignado por: %s",
                maintenance.getCode(),
                maintenance.getEquipment().getName(),
                maintenance.getMaintenanceType().getName(),
                maintenance.getPriority().name(),
                maintenance.getDescription(),
                creator.getName()
            );
            
            productionEmailService.sendActionConfirmation(
                responsible.getEmail(),
                responsible.getName(),
                action,
                details
            );
            
            logger.info("Maintenance assignment email sent to: {}", responsible.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send maintenance assignment email to: {}", responsible.getEmail(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getMaintenanceByReviewStatus(Maintenance.ReviewStatus reviewStatus) {
        return maintenanceRepository.findByReviewStatus(reviewStatus).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getMaintenanceByResponsibleUser(UUID userId) {
        return maintenanceRepository.findByResponsibleId(userId).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getMaintenanceByRequestedUser(UUID userId) {
        return maintenanceRepository.findByRequestedById(userId).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getPendingReviewMaintenance() {
        return maintenanceRepository.findByReviewStatus(Maintenance.ReviewStatus.PENDING).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getApprovedMaintenance() {
        return maintenanceRepository.findByReviewStatus(Maintenance.ReviewStatus.APPROVED).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getInProgressMaintenance() {
        return maintenanceRepository.findByReviewStatus(Maintenance.ReviewStatus.IN_PROGRESS).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getRejectedMaintenance() {
        return maintenanceRepository.findByReviewStatus(Maintenance.ReviewStatus.REJECTED).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getMaintenanceCreatedByUser(User user) {
        return maintenanceRepository.findByRequestedByAndScheduledMaintenanceIsNullOrderByCreatedAtDesc(user).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getMaintenanceAssignedToUser(User user) {
        return maintenanceRepository.findByResponsibleAndScheduledMaintenanceIsNullOrderByCreatedAtDesc(user).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getMyMaintenance(User user) {
        // Obtener mantenimientos creados por el usuario
        List<Maintenance> createdMaintenance = maintenanceRepository.findByRequestedByOrderByCreatedAtDesc(user);
        
        // Obtener mantenimientos asignados al usuario
        List<Maintenance> assignedMaintenance = maintenanceRepository.findByResponsibleOrderByCreatedAtDesc(user);
        
        // Combinar ambas listas y eliminar duplicados
        List<Maintenance> allMaintenance = new ArrayList<>();
        allMaintenance.addAll(createdMaintenance);
        allMaintenance.addAll(assignedMaintenance);
        
        // Ordenar por fecha de creación (más reciente primero)
        allMaintenance.sort((m1, m2) -> m2.getCreatedAt().compareTo(m1.getCreatedAt()));
        
        return allMaintenance.stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public Maintenance approveMaintenance(UUID maintenanceId, User currentUser) {
        // Find existing maintenance
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));
        
        // Validate that maintenance is pending review
        if (maintenance.getReviewStatus() != Maintenance.ReviewStatus.PENDING) {
            throw new RuntimeException("Maintenance is not pending review");
        }
        
        // Validate that the current user is the creator (who assigned the maintenance)
        if (!maintenance.getRequestedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the creator can approve maintenance");
        }
        
        // Update maintenance status to approved
        maintenance.setReviewStatus(Maintenance.ReviewStatus.APPROVED);
        maintenance.setRejectionReason(null);
        maintenance.setReviewedBy(currentUser);
        maintenance.setReviewedAt(LocalDateTime.now());
        maintenance.setUpdatedAt(LocalDateTime.now());

        Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log
        createMaintenanceAuditLog(updatedMaintenance, currentUser);

        // Send notification to the responsible person about approval
        sendMaintenanceApprovalNotification(updatedMaintenance, currentUser);

        // Create notice notification for the responsible person
        noticeService.createMaintenanceApprovalNotice(updatedMaintenance, maintenance.getResponsible());

        return updatedMaintenance;
    }

    @Transactional
    public Maintenance rejectMaintenance(UUID maintenanceId, String rejectionReason, User currentUser) {
        // Find existing maintenance
        Maintenance maintenance = maintenanceRepository.findById(maintenanceId)
            .orElseThrow(() -> new EntityNotFoundException("Maintenance not found"));
        
        // Validate that maintenance is pending review
        if (maintenance.getReviewStatus() != Maintenance.ReviewStatus.PENDING) {
            throw new RuntimeException("Maintenance is not pending review");
        }
        
        // Validate that the current user is the creator (who assigned the maintenance)
        if (!maintenance.getRequestedBy().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only the creator can reject maintenance");
        }
        
        // Validate rejection reason
        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new RuntimeException("Rejection reason is required");
        }
        
        // Update maintenance status back to in progress for retry
        maintenance.setReviewStatus(Maintenance.ReviewStatus.IN_PROGRESS);
        maintenance.setRejectionReason(rejectionReason);
        maintenance.setReviewedBy(currentUser);
        maintenance.setReviewedAt(LocalDateTime.now());
        maintenance.setUpdatedAt(LocalDateTime.now());

        Maintenance updatedMaintenance = maintenanceRepository.save(maintenance);

        // Create audit log
        createMaintenanceAuditLog(updatedMaintenance, currentUser);

        // Send notification to the responsible person about rejection with reason
        sendMaintenanceRejectionNotification(updatedMaintenance, currentUser, rejectionReason);

        // Create notice notification for the responsible person
        noticeService.createMaintenanceRejectionNotice(updatedMaintenance, maintenance.getResponsible(), rejectionReason);

        return updatedMaintenance;
    }

    private void sendMaintenanceApprovalNotification(Maintenance maintenance, User reviewer) {
        // Send real email notification using ProductionEmailService
        try {
            String action = "Mantenimiento Aprobado";
            String details = String.format(
                "Tu mantenimiento %s para el equipo %s ha sido APROBADO por %s. " +
                "El mantenimiento está listo para proceder.",
                maintenance.getCode(),
                maintenance.getEquipment().getName(),
                reviewer.getName()
            );
            
            productionEmailService.sendActionConfirmation(
                maintenance.getResponsible().getEmail(),
                maintenance.getResponsible().getName(),
                action,
                details
            );
            
            logger.info("Maintenance approval email sent to: {}", maintenance.getResponsible().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send maintenance approval email to: {}", maintenance.getResponsible().getEmail(), e);
        }
    }

    private void sendMaintenanceRejectionNotification(Maintenance maintenance, User reviewer, String rejectionReason) {
        // Send real email notification using ProductionEmailService
        try {
            String action = "Mantenimiento Rechazado";
            String details = String.format(
                "Tu mantenimiento %s para el equipo %s ha sido RECHAZADO por %s. " +
                "Razón del rechazo: %s. " +
                "El mantenimiento ha sido devuelto a estado IN_PROGRESS para que puedas hacer los ajustes necesarios.",
                maintenance.getCode(),
                maintenance.getEquipment().getName(),
                reviewer.getName(),
                rejectionReason
            );
            
            productionEmailService.sendActionConfirmation(
                maintenance.getResponsible().getEmail(),
                maintenance.getResponsible().getName(),
                action,
                details
            );
            
            logger.info("Maintenance rejection email sent to: {}", maintenance.getResponsible().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send maintenance rejection email to: {}", maintenance.getResponsible().getEmail(), e);
        }
    }
} 