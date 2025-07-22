package com.labMetricas.LabMetricas.auditLog.service;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.model.dto.AuditLogDto;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public List<AuditLogDto> getAllLogs() {
        return auditLogRepository.findAll().stream()
            .map(log -> new AuditLogDto(
                log.getAction(),
                log.getCreatedAt(),
                log.getUser() != null ? log.getUser().getEmail() : null,
                log.getUser() != null ? log.getUser().getName() : null
            ))
            .collect(Collectors.toList());
    }

    public List<AuditLogDto> getLogsByUserEmail(String email) {
        return auditLogRepository.findAll().stream()
            .filter(log -> log.getUser() != null && email.equalsIgnoreCase(log.getUser().getEmail()))
            .map(log -> new AuditLogDto(
                log.getAction(),
                log.getCreatedAt(),
                log.getUser().getEmail(),
                log.getUser().getName()
            ))
            .collect(Collectors.toList());
    }
} 