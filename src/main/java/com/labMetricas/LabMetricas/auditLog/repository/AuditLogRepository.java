package com.labMetricas.LabMetricas.auditLog.repository;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
} 