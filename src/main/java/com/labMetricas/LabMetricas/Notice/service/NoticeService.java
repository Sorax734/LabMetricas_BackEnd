package com.labMetricas.LabMetricas.Notice.service;

import com.labMetricas.LabMetricas.Notice.model.Notice;
import com.labMetricas.LabMetricas.Notice.model.dto.NoticeDto;
import com.labMetricas.LabMetricas.Notice.repository.NoticeRepository;
import com.labMetricas.LabMetricas.config.ProductionEmailService;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.sentEmail.model.SentEmail;
import com.labMetricas.LabMetricas.sentEmail.repository.SentEmailRepository;
import com.labMetricas.LabMetricas.user.model.User;
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

@Service
public class NoticeService {
    private static final Logger logger = LoggerFactory.getLogger(NoticeService.class);

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private SentEmailRepository sentEmailRepository;

    @Autowired
    private ProductionEmailService productionEmailService;

    @Transactional
    public Notice createMaintenanceNotice(Maintenance maintenance, User responsibleUser) {
        Notice notice = new Notice();
        notice.setTitle("Nuevo Mantenimiento Asignado");
        notice.setDescription(generateMaintenanceDescription(maintenance));
        notice.setStatus(true);
        notice.setCreatedBy(responsibleUser); // La notificación es para el usuario responsable
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        
        Notice savedNotice = noticeRepository.save(notice);
        
        // Send email notification
        sendNoticeEmail(savedNotice);
        
        return savedNotice;
    }

    @Transactional
    public Notice createMaintenanceReviewRequestNotice(Maintenance maintenance, User creator) {
        Notice notice = new Notice();
        notice.setTitle("Mantenimiento Enviado para Revisión");
        notice.setDescription(generateReviewRequestDescription(maintenance));
        notice.setStatus(true);
        notice.setCreatedBy(creator); // La notificación es para el creador
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        
        Notice savedNotice = noticeRepository.save(notice);
        
        // Send email notification
        sendNoticeEmail(savedNotice);
        
        return savedNotice;
    }

    @Transactional
    public Notice createMaintenanceApprovalNotice(Maintenance maintenance, User responsibleUser) {
        Notice notice = new Notice();
        notice.setTitle("Mantenimiento Aprobado");
        notice.setDescription(generateApprovalDescription(maintenance));
        notice.setStatus(true);
        notice.setCreatedBy(responsibleUser); // La notificación es para el responsable
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        
        Notice savedNotice = noticeRepository.save(notice);
        
        // Send email notification
        sendNoticeEmail(savedNotice);
        
        return savedNotice;
    }

    @Transactional
    public Notice createMaintenanceRejectionNotice(Maintenance maintenance, User responsibleUser, String rejectionReason) {
        Notice notice = new Notice();
        notice.setTitle("Mantenimiento Rechazado");
        notice.setDescription(generateRejectionDescription(maintenance, rejectionReason));
        notice.setStatus(true);
        notice.setCreatedBy(responsibleUser); // La notificación es para el responsable
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        
        Notice savedNotice = noticeRepository.save(notice);
        
        // Send email notification
        sendNoticeEmail(savedNotice);
        
        return savedNotice;
    }

    private void sendNoticeEmail(Notice notice) {
        try {
            // Send real email notification using ProductionEmailService
            String subject = "Nueva Notificación: " + notice.getTitle();
            String htmlContent = createNoticeEmailBody(notice);
            
            productionEmailService.sendCustomEmail(
                notice.getCreatedBy().getEmail(),
                subject,
                htmlContent
            );
            
            logger.info("Notice email sent to: {}", notice.getCreatedBy().getEmail());
        } catch (Exception e) {
            logger.error("Failed to send notice email to: {}", notice.getCreatedBy().getEmail(), e);
        }
    }

    private String createNoticeEmailBody(Notice notice) {
        return String.format(
            "<html>" +
            "<head>" +
            "    <style>" +
            "        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
            "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
            "        .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }" +
            "        .content { background-color: #f8f9fa; padding: 20px; border-radius: 0 0 5px 5px; }" +
            "        .title { color: #007bff; font-weight: bold; }" +
            "    </style>" +
            "</head>" +
            "<body>" +
            "    <div class='container'>" +
            "        <div class='header'>" +
            "            <h1>LabMetricas - Notificación</h1>" +
            "        </div>" +
            "        <div class='content'>" +
            "            <h2 class='title'>%s</h2>" +
            "            <p><strong>Descripción:</strong> %s</p>" +
            "            <p><strong>Fecha de creación:</strong> %s</p>" +
            "            <p>Por favor, revisa tu panel de notificaciones para más detalles.</p>" +
            "            <p>Saludos,<br>Sistema de Gestión de Mantenimiento</p>" +
            "        </div>" +
            "    </div>" +
            "</body>" +
            "</html>",
            notice.getTitle(),
            notice.getDescription(),
            notice.getCreatedAt().toString()
        );
    }

    private String generateMaintenanceDescription(Maintenance maintenance) {
        return String.format(
            "Se ha asignado un nuevo mantenimiento para el equipo %s (Código: %s). " +
            "Tipo de mantenimiento: %s. " +
            "Prioridad: %s. " +
            "Descripción: %s. " +
            "Código de mantenimiento: %s",
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode(),
            maintenance.getMaintenanceType().getName(),
            maintenance.getPriority().name(),
            maintenance.getDescription(),
            maintenance.getCode()
        );
    }

    private String generateReviewRequestDescription(Maintenance maintenance) {
        return String.format(
            "El mantenimiento %s para el equipo %s (Código: %s) ha sido enviado para revisión. " +
            "Por favor, revisa y aprueba o rechaza el mantenimiento según corresponda.",
            maintenance.getCode(),
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode()
        );
    }

    private String generateApprovalDescription(Maintenance maintenance) {
        return String.format(
            "El mantenimiento %s para el equipo %s (Código: %s) ha sido APROBADO. " +
            "El mantenimiento está listo para proceder.",
            maintenance.getCode(),
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode()
        );
    }

    private String generateRejectionDescription(Maintenance maintenance, String rejectionReason) {
        return String.format(
            "El mantenimiento %s para el equipo %s (Código: %s) ha sido RECHAZADO. " +
            "Razón del rechazo: %s. " +
            "El mantenimiento ha sido devuelto a estado IN_PROGRESS para que puedas hacer los ajustes necesarios.",
            maintenance.getCode(),
            maintenance.getEquipment().getName(),
            maintenance.getEquipment().getCode(),
            rejectionReason
        );
    }

    @Transactional(readOnly = true)
    public List<NoticeDto> getActiveNoticesByUser(User user) {
        return noticeRepository.findByCreatedByAndStatusTrueAndDeletedAtIsNullOrderByCreatedAtDesc(user)
            .stream()
            .map(NoticeDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeDto> getAllNoticesByUser(User user) {
        return noticeRepository.findByCreatedByAndDeletedAtIsNullOrderByCreatedAtDesc(user)
            .stream()
            .map(NoticeDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeDto> getAllActiveNotices() {
        return noticeRepository.findByStatusTrueAndDeletedAtIsNullOrderByCreatedAtDesc()
            .stream()
            .map(NoticeDto::new)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NoticeDto> getAllNotices() {
        return noticeRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()
            .stream()
            .map(NoticeDto::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deleteNoticesByMaintenanceCode(String maintenanceCode) {
        // Buscar y eliminar todas las notificaciones relacionadas con un mantenimiento específico
        List<Notice> notices = noticeRepository.findByDescriptionContainingAndDeletedAtIsNull(maintenanceCode);
        
        for (Notice notice : notices) {
            notice.setDeletedAt(LocalDateTime.now());
            notice.setUpdatedAt(LocalDateTime.now());
            noticeRepository.save(notice);
        }
    }
} 