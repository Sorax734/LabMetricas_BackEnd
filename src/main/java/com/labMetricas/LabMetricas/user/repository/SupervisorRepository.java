package com.labMetricas.LabMetricas.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.labMetricas.LabMetricas.user.model.User;
import java.util.UUID;

@Repository
public interface SupervisorRepository extends JpaRepository<User, UUID> {
    // Placeholder for future specific Supervisor repository methods
} 