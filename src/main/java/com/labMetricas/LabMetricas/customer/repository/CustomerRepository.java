package com.labMetricas.LabMetricas.customer.repository;

import com.labMetricas.LabMetricas.customer.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEmail(String email);
    Optional<Customer> findByNif(String nif);
    boolean existsByEmail(String email);
    boolean existsByNif(String nif);
    
    // New method to find active customers
    List<Customer> findByStatusTrue();
} 