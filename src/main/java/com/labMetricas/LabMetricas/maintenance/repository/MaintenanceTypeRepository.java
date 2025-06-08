package com.labMetricas.LabMetricas.maintenance.repository;

import com.labMetricas.LabMetricas.maintenance.model.MaintenanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaintenanceTypeRepository extends JpaRepository<MaintenanceType, UUID> {
    Optional<MaintenanceType> findByName(String name);
} 