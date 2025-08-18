package com.labMetricas.LabMetricas.maintenance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "maintenance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Maintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "status", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean status = true;

    @Column(name = "description", columnDefinition = "VARCHAR(1000)", length = 1000)
    private String description;

    @Column(name = "code", columnDefinition = "VARCHAR(30)", nullable = false, length = 30, unique = true)
    private String code;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.MEDIUM;

    // Nuevos campos para el flujo de aprobación
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private ReviewStatus reviewStatus = ReviewStatus.IN_PROGRESS;

    @Column(name = "rejection_reason", columnDefinition = "VARCHAR(500)", length = 500)
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "requested_by")
    private User requestedBy;

    @ManyToOne
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @ManyToOne
    @JoinColumn(name = "maintenance_type", nullable = false)
    private MaintenanceType maintenanceType;

    @ManyToOne
    @JoinColumn(name = "responsible", nullable = false)
    private User responsible;

    @ManyToOne
    @JoinColumn(name = "equipment", nullable = false)
    private Equipment equipment;

    @OneToOne(mappedBy = "maintenance")
    @JsonIgnore
    private ScheduledMaintenance scheduledMaintenance;

    // Priority enum
    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // Review Status enum
    public enum ReviewStatus {
        IN_PROGRESS,  // En progreso (automático al crear)
        PENDING,      // Pendiente de revisión por el creador
        APPROVED,     // Aprobado por el creador
        REJECTED      // Rechazado por el creador
    }
} 