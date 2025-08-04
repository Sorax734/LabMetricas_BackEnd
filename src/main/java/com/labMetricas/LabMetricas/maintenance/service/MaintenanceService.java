package com.labMetricas.LabMetricas.maintenance.service;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
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
        maintenance.setCode(generateMaintenanceCode());
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

        return updatedMaintenance;
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

        return deletedMaintenance;
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
        // Create a sent email record for the review request notification
        SentEmail sentEmail = new SentEmail();
        sentEmail.setSubject("Maintenance Review Request: " + maintenance.getCode());
        sentEmail.setBody(createMaintenanceReviewRequestBody(maintenance, creator));
        sentEmail.setUser(reviewer);
        sentEmail.setCreatedAt(LocalDateTime.now());

        sentEmailRepository.save(sentEmail);
    }

    private String createMaintenanceReviewRequestBody(Maintenance maintenance, User creator) {
        return String.format(
            "Dear %s,\n\n" +
            "A maintenance request has been submitted for your review:\n\n" +
            "Maintenance Code: %s\n" +
            "Equipment: %s (Code: %s)\n" +
            "Description: %s\n" +
            "Maintenance Type: %s\n" +
            "Priority: %s\n" +
            "Responsible: %s\n" +
            "Requested by: %s\n\n" +
            "Please review and approve or reject this maintenance request.\n\n" +
            "Best regards,\n" +
            "Maintenance Management System",
            maintenance.getReviewedBy() != null ? maintenance.getReviewedBy().getName() : "Administrator",
            maintenance.getCode(),
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode(),
            maintenance.getDescription(),
            maintenance.getMaintenanceType().getName(),
            maintenance.getPriority().name(),
            maintenance.getResponsible().getName(),
            creator.getName()
        );
    }



    private void sendMaintenanceAssignmentNotification(Maintenance maintenance, User responsible, User creator) {
        // Create a sent email record for the maintenance assignment notification
        SentEmail sentEmail = new SentEmail();
        sentEmail.setSubject("New Maintenance Assignment: " + maintenance.getCode());
        sentEmail.setBody(createMaintenanceAssignmentBody(maintenance, creator));
        sentEmail.setUser(responsible);
        sentEmail.setCreatedAt(LocalDateTime.now());

        sentEmailRepository.save(sentEmail);
    }

    private String createMaintenanceAssignmentBody(Maintenance maintenance, User creator) {
        return String.format(
            "Dear %s,\n\n" +
            "You have been assigned a new maintenance request:\n\n" +
            "Maintenance Code: %s\n" +
            "Equipment: %s (Code: %s)\n" +
            "Description: %s\n" +
            "Maintenance Type: %s\n" +
            "Priority: %s\n" +
            "Requested by: %s\n\n" +
            "Please review and take necessary actions.\n\n" +
            "Best regards,\n" +
            "Maintenance Management System",
            maintenance.getResponsible().getName(),
            maintenance.getCode(),
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode(),
            maintenance.getDescription(),
            maintenance.getMaintenanceType().getName(),
            maintenance.getPriority().name(),
            creator.getName()
        );
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
        return maintenanceRepository.findByRequestedByOrderByCreatedAtDesc(user).stream()
            .map(MaintenanceDetailDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MaintenanceDetailDto> getMaintenanceAssignedToUser(User user) {
        return maintenanceRepository.findByResponsibleOrderByCreatedAtDesc(user).stream()
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
        // Create a sent email record for the approval notification
        SentEmail sentEmail = new SentEmail();
        sentEmail.setSubject("Maintenance Approved: " + maintenance.getCode());
        sentEmail.setBody(createMaintenanceApprovalBody(maintenance, reviewer));
        sentEmail.setUser(maintenance.getResponsible());
        sentEmail.setCreatedAt(LocalDateTime.now());

        sentEmailRepository.save(sentEmail);
    }

    private String createMaintenanceApprovalBody(Maintenance maintenance, User reviewer) {
        return String.format(
            "Dear %s,\n\n" +
            "Your maintenance request has been APPROVED:\n\n" +
            "Maintenance Code: %s\n" +
            "Equipment: %s (Code: %s)\n" +
            "Description: %s\n" +
            "Approved by: %s\n" +
            "Approval Date: %s\n\n" +
            "Your maintenance request has been approved and is ready to proceed.\n\n" +
            "Best regards,\n" +
            "Maintenance Management System",
            maintenance.getResponsible().getName(),
            maintenance.getCode(),
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode(),
            maintenance.getDescription(),
            reviewer.getName(),
            maintenance.getReviewedAt()
        );
    }

    private void sendMaintenanceRejectionNotification(Maintenance maintenance, User reviewer, String rejectionReason) {
        // Create a sent email record for the rejection notification
        SentEmail sentEmail = new SentEmail();
        sentEmail.setSubject("Maintenance Rejected: " + maintenance.getCode());
        sentEmail.setBody(createMaintenanceRejectionBody(maintenance, reviewer, rejectionReason));
        sentEmail.setUser(maintenance.getResponsible());
        sentEmail.setCreatedAt(LocalDateTime.now());

        sentEmailRepository.save(sentEmail);
    }

    private String createMaintenanceRejectionBody(Maintenance maintenance, User reviewer, String rejectionReason) {
        return String.format(
            "Dear %s,\n\n" +
            "Your maintenance request has been REJECTED:\n\n" +
            "Maintenance Code: %s\n" +
            "Equipment: %s (Code: %s)\n" +
            "Description: %s\n" +
            "Rejected by: %s\n" +
            "Rejection Date: %s\n" +
            "Rejection Reason: %s\n\n" +
            "The maintenance has been returned to IN_PROGRESS status so you can make the necessary adjustments and submit it again for review.\n\n" +
            "Best regards,\n" +
            "Maintenance Management System",
            maintenance.getResponsible().getName(),
            maintenance.getCode(),
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode(),
            maintenance.getDescription(),
            reviewer.getName(),
            maintenance.getReviewedAt(),
            rejectionReason
        );
    }
} 