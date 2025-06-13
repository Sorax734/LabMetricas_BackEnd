package com.labMetricas.LabMetricas.user.repository;

import com.labMetricas.LabMetricas.role.model.Role;
import com.labMetricas.LabMetricas.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    // Basic methods from model UserRepository
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    // Additional methods from previous repository
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.email = :email")
    Optional<User> findByEmailWithRole(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role")
    List<User> findAllWithRoles();

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.role WHERE u.id = :id")
    Optional<User> findByIdWithRole(@Param("id") UUID id);

    void deleteByEmail(String email);

    List<User> findByRole(Role role);
} 