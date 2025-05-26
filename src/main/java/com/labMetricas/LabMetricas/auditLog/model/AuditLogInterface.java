package com.labMetricas.LabMetricas.auditLog.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuditLogInterface extends JpaRepository<AuditLog, Long> {
    Optional<AuditLog> findById(Long id);
    
}
