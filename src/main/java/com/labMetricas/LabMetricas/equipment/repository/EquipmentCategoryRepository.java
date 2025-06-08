package com.labMetricas.LabMetricas.equipment.repository;

import com.labMetricas.LabMetricas.equipment.model.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, UUID> {
    Optional<EquipmentCategory> findByName(String name);
} 