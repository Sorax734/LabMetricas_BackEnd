package com.labMetricas.LabMetricas.maintenance.repository;

import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceRepository extends JpaRepository<Maintenance, UUID> {
    // Find maintenance by review status
    List<Maintenance> findByReviewStatus(Maintenance.ReviewStatus reviewStatus);
    
    // Find maintenance by responsible user
    List<Maintenance> findByResponsibleId(UUID responsibleId);
    
    // Find maintenance by requested user
    List<Maintenance> findByRequestedById(UUID requestedById);
    
    // Find maintenance by reviewer
    List<Maintenance> findByReviewedById(UUID reviewedById);
    
    // Find pending maintenance for a specific reviewer
    List<Maintenance> findByReviewStatusAndReviewedById(Maintenance.ReviewStatus reviewStatus, UUID reviewedById);
} 