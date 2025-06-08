package com.labMetricas.LabMetricas.maintenance.repository;

import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, Integer> {
    // Custom query methods can be added here if needed
} 