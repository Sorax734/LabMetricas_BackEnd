package com.labMetricas.LabMetricas.equipment.repository;

import com.labMetricas.LabMetricas.equipment.model.EquipmentCategory;
import com.labMetricas.LabMetricas.equipment.model.MaintenanceProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MaintenanceProviderRepository extends JpaRepository<MaintenanceProvider, Integer> {
    Optional<MaintenanceProvider> findByName(String name);
}