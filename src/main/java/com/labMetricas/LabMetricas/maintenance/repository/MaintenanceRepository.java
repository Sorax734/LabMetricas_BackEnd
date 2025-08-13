package com.labMetricas.LabMetricas.maintenance.repository;

import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.user.model.User;
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

    // Find maintenance created by user (ordered by creation date desc)
    List<Maintenance> findByRequestedByOrderByCreatedAtDesc(User requestedBy);

    // Find maintenance assigned to user (ordered by creation date desc)
    List<Maintenance> findByResponsibleOrderByCreatedAtDesc(User responsible);

    // Find maintenance created by user (ordered by creation date desc)
    List<Maintenance> findByRequestedByAndScheduledMaintenanceIsNullOrderByCreatedAtDesc(User requestedBy); // Solicitudes
    
    // Find maintenance assigned to user (ordered by creation date desc)
    List<Maintenance> findByResponsibleAndScheduledMaintenanceIsNullOrderByCreatedAtDesc(User responsible); // Solicitudes

    // Find maintenance created by user (ordered by creation date desc)
    List<Maintenance> findByRequestedByAndScheduledMaintenanceIsNotNullOrderByCreatedAtDesc(User requestedBy); // Programados

    // Find maintenance assigned to user (ordered by creation date desc)
    List<Maintenance> findByResponsibleAndScheduledMaintenanceIsNotNullOrderByCreatedAtDesc(User responsible); // Programados
    
    // Count maintenance by program type (P or NP)
    long countByScheduledMaintenanceIsNotNull(); // Count programmed maintenance
    long countByScheduledMaintenanceIsNull(); // Count non-programmed maintenance
}