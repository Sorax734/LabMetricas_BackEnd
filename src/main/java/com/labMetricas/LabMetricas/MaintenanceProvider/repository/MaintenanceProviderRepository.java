package com.labMetricas.LabMetricas.MaintenanceProvider.repository;

import com.labMetricas.LabMetricas.MaintenanceProvider.model.MaintenanceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MaintenanceProviderRepository extends JpaRepository<MaintenanceProvider, UUID> {
    // Find by name (case-insensitive)
    Optional<MaintenanceProvider> findByNameIgnoreCase(String name);

    // Find all active maintenance providers
    List<MaintenanceProvider> findByStatusTrue();

    // Check if a maintenance provider name already exists
    boolean existsByNameIgnoreCase(String name);
}