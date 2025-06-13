package com.labMetricas.LabMetricas.user.repository;

import com.labMetricas.LabMetricas.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OperadorRepository extends JpaRepository<User, UUID> {
    // Specific methods for Operador if needed
} 