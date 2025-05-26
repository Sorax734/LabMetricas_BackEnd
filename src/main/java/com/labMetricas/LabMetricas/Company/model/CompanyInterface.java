package com.labMetricas.LabMetricas.Company.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CompanyInterface extends JpaRepository<Company, Long> {
    Optional<Company> findById(Long id);
}
