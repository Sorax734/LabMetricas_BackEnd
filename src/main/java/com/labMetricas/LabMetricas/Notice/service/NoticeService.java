package com.labMetricas.LabMetricas.Notice.service;

import com.labMetricas.LabMetricas.Notice.model.Notice;
import com.labMetricas.LabMetricas.Notice.model.dto.NoticeDto;
import com.labMetricas.LabMetricas.Notice.repository.NoticeRepository;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.sentEmail.model.SentEmail;
import com.labMetricas.LabMetricas.sentEmail.repository.SentEmailRepository;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NoticeService {

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private SentEmailRepository sentEmailRepository;

    @Transactional
    public Notice createMaintenanceNotice(Maintenance maintenance, User user) {
        Notice notice = new Notice();
        notice.setTitle("Nuevo Mantenimiento Asignado");
        notice.setDescription(generateMaintenanceDescription(maintenance));
        notice.setStatus(true);
        notice.setCreatedBy(user);
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        
        Notice savedNotice = noticeRepository.save(notice);
        
        // Send email notification
        sendNoticeEmail(savedNotice);
        
        return savedNotice;
    }

    private void sendNoticeEmail(Notice notice) {
        try {
            SentEmail sentEmail = new SentEmail();
            sentEmail.setSubject("Nueva Notificación: " + notice.getTitle());
            sentEmail.setBody(createNoticeEmailBody(notice));
            sentEmail.setUser(notice.getCreatedBy());
            sentEmail.setCreatedAt(LocalDateTime.now());
            
            sentEmailRepository.save(sentEmail);
        } catch (Exception e) {
            // Log error but don't fail the notice creation
            System.err.println("Error sending notice email: " + e.getMessage());
        }
    }

    private String createNoticeEmailBody(Notice notice) {
        return String.format(
            "Hola %s,\n\n" +
            "Has recibido una nueva notificación:\n\n" +
            "Título: %s\n" +
            "Descripción: %s\n" +
            "Fecha de creación: %s\n\n" +
            "Por favor, revisa tu panel de notificaciones para más detalles.\n\n" +
            "Saludos,\n" +
            "Sistema de Gestión de Mantenimiento",
            notice.getCreatedBy().getName(),
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