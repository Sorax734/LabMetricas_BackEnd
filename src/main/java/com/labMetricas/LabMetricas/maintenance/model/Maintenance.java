package com.labMetricas.LabMetricas.maintenance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "maintenance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Maintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_maintenance")
    private Integer id;

    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", nullable = false, length = 1000)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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
} 