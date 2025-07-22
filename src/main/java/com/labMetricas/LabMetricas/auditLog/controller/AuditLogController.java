package com.labMetricas.LabMetricas.auditLog.controller;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.model.dto.AuditLogDto;
import com.labMetricas.LabMetricas.auditLog.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public List<AuditLogDto> getAllLogs() {
        return auditLogService.getAllLogs();
    }

    @GetMapping("/user/{email}")
    public List<AuditLogDto> getLogsByUserEmail(@PathVariable("email") String email) {
        return auditLogService.getLogsByUserEmail(email);
    }
} 