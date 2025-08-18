package com.labMetricas.LabMetricas.equipment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.EquipmentCategory.model.EquipmentCategory;
import com.labMetricas.LabMetricas.MaintenanceProvider.model.MaintenanceProvider;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
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
@Table(name = "equipment", 
    indexes = {
        @Index(name = "equipment_name_index", columnList = "name"),
        @Index(name = "equipment_status_index", columnList = "status"),
        //@Index(name = "equipment_location_index", columnList = "location")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "status", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean status = true;

    @Column(name = "name", columnDefinition = "VARCHAR(100)", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "code", columnDefinition = "VARCHAR(10)", nullable = false, length = 10)
    private String code;

    @Column(name = "serial_number", columnDefinition = "VARCHAR(24)", nullable = false, length = 24)
    private String serialNumber;

    @Column(name = "location", columnDefinition = "TEXT", nullable = false)
    private String location;

    @Column(name = "brand", columnDefinition = "VARCHAR(50)", nullable = false, length = 50)
    private String brand;

    @Column(name = "model", columnDefinition = "VARCHAR(100)", nullable = false, length = 100)
    private String model;

    @Column(name = "remarks", columnDefinition = "VARCHAR(1000)", length = 1000)
    private String remarks;

    @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at", columnDefinition = "TIMESTAMP")
    private LocalDateTime deletedAt;

    @ManyToOne
    @JoinColumn(name = "assigned_to", nullable = false)
    private User assignedTo;

    @ManyToOne
    @JoinColumn(name = "equipment_category", nullable = false)
    private EquipmentCategory equipmentCategory;

    @ManyToOne
    @JoinColumn(name = "maintenance_provider", nullable = false)
    private MaintenanceProvider maintenanceProvider;

    @OneToMany(mappedBy = "equipment")
    @JsonIgnore
    private List<Maintenance> maintenances;
} 