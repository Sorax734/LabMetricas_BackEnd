package com.labMetricas.LabMetricas.document.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Optional;

public interface DocumentInterface extends JpaRepository<Document, Long> {
    Optional<Document> findById(Long id);

}
