package com.labMetricas.LabMetricas.documentVersion.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentVersionInterface extends JpaRepository<DocumentVersion, Long> {
    Optional<DocumentVersion> findById(Long id);
}
