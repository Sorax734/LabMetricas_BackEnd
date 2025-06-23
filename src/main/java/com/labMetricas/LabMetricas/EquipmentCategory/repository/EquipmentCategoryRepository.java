package com.labMetricas.LabMetricas.EquipmentCategory.repository;

import com.labMetricas.LabMetricas.EquipmentCategory.model.EquipmentCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentCategoryRepository extends JpaRepository<EquipmentCategory, UUID> {
    // Find by name (case-insensitive)
    Optional<EquipmentCategory> findByNameIgnoreCase(String name);

    // Find all active categories
    List<EquipmentCategory> findByStatusTrue();

    // Check if a category name already exists
    boolean existsByNameIgnoreCase(String name);
} 