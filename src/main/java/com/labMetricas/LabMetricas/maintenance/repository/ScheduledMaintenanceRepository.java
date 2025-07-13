package com.labMetricas.LabMetricas.maintenance.repository;

import com.labMetricas.LabMetricas.maintenance.model.ScheduledMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ScheduledMaintenanceRepository extends JpaRepository<ScheduledMaintenance, UUID> {
    // Custom query methods can be added here if needed
} 