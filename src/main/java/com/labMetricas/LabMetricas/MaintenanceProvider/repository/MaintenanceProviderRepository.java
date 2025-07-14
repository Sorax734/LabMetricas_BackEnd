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

    // Find by email (case-insensitive)
    Optional<MaintenanceProvider> findByEmailIgnoreCase(String email);

    // Find by NIF (case-insensitive)
    Optional<MaintenanceProvider> findByNifIgnoreCase(String nif);

    // Check if a maintenance provider email already exists
    boolean existsByEmailIgnoreCase(String email);

    // Check if a maintenance provider NIF already exists
    boolean existsByNifIgnoreCase(String nif);
}