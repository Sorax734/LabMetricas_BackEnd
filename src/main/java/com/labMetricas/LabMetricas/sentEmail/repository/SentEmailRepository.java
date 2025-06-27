package com.labMetricas.LabMetricas.sentEmail.repository;

import com.labMetricas.LabMetricas.sentEmail.model.SentEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
@Repository
public interface SentEmailRepository extends JpaRepository<SentEmail, Integer> {
} 