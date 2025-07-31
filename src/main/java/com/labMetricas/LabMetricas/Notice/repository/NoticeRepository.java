package com.labMetricas.LabMetricas.Notice.repository;

import com.labMetricas.LabMetricas.Notice.model.Notice;
import com.labMetricas.LabMetricas.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, UUID> {
    
    // Find active notices by user (status = true and not deleted)
    List<Notice> findByCreatedByAndStatusTrueAndDeletedAtIsNullOrderByCreatedAtDesc(User user);
    
    // Find all notices by user (not deleted)
    List<Notice> findByCreatedByAndDeletedAtIsNullOrderByCreatedAtDesc(User user);
    
    // Find all active notices (status = true and not deleted)
    List<Notice> findByStatusTrueAndDeletedAtIsNullOrderByCreatedAtDesc();
    
    // Find all notices (not deleted)
    List<Notice> findByDeletedAtIsNullOrderByCreatedAtDesc();
    
    // Find notices by maintenance code in description
    @Query("SELECT n FROM Notice n WHERE n.description LIKE %:maintenanceCode% AND n.deletedAt IS NULL")
    List<Notice> findByDescriptionContainingAndDeletedAtIsNull(@Param("maintenanceCode") String maintenanceCode);
} 