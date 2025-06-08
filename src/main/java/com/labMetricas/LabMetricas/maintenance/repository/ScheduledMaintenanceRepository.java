package com.labMetricas.LabMetricas.maintenance.repository;

import com.labMetricas.LabMetricas.maintenance.model.ScheduledMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduledMaintenanceRepository extends JpaRepository<ScheduledMaintenance, Integer> {
    // Custom query methods can be added here if needed
} 