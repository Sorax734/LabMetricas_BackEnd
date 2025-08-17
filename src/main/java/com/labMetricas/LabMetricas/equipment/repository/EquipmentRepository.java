package com.labMetricas.LabMetricas.equipment.repository;

import com.labMetricas.LabMetricas.equipment.model.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {
    Optional<Equipment> findByName(String name);
    boolean existsByName(String name);
} 