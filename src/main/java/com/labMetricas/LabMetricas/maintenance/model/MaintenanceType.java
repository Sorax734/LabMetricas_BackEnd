package com.labMetricas.LabMetricas.maintenance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "maintenance_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceType {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "status", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean status = true;

    @Column(name = "name", columnDefinition = "VARCHAR(50)", nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "VARCHAR(255)", length = 255)
    private String description;

    @Column(name = "is_preventive", columnDefinition = "TINYINT(1)")
    private Boolean isPreventive = false;

    @Column(name = "is_corrective", columnDefinition = "TINYINT(1)")
    private Boolean isCorrective = false;

    @Column(name = "is_calibration", columnDefinition = "TINYINT(1)")
    private Boolean isCalibration = false;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "maintenanceType")
    @JsonIgnore
    private List<Maintenance> maintenances;

    // Constructor for quick creation
    public MaintenanceType(String name, String description, 
                           Boolean isPreventive, 
                           Boolean isCorrective, 
                           Boolean isCalibration) {
        this.name = name;
        this.description = description;
        this.isPreventive = isPreventive;
        this.isCorrective = isCorrective;
        this.isCalibration = isCalibration;
    }
} 