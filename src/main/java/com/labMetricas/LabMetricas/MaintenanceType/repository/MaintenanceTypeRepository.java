package com.labMetricas.LabMetricas.MaintenanceType.repository;

import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaintenanceTypeRepository extends JpaRepository<MaintenanceType, UUID> {
    Optional<MaintenanceType> findByName(String name);

    @Query("SELECT mt FROM MaintenanceType mt WHERE mt.status = true")
    List<MaintenanceType> findAllActive();

    // New method to find all maintenance types regardless of status
    List<MaintenanceType> findAll();
} 