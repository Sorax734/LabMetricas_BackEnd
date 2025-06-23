package com.labMetricas.LabMetricas.MaintenanceProvider.repository;

import com.labMetricas.LabMetricas.MaintenanceProvider.model.MaintenanceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaintenanceProviderRepository extends JpaRepository<MaintenanceProvider, UUID> {
    Optional<MaintenanceProvider> findByName(String name);
    Optional<MaintenanceProvider> findByNameIgnoreCase(String name);
}