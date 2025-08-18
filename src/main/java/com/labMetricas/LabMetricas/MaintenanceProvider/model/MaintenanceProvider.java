package com.labMetricas.LabMetricas.MaintenanceProvider.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.equipment.model.Equipment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "maintenance_provider",
        indexes = {
                @Index(name = "maintenance_provider_name_index", columnList = "name"),
                @Index(name = "maintenance_provider_status_index", columnList = "status")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceProvider {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "status", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean status = true;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "address", columnDefinition = "VARCHAR(255)", nullable = true, length = 255)
    private String address;

    @Column(name = "phone", columnDefinition = "VARCHAR(20)", nullable = true, length = 20)
    private String phone;

    @Column(name = "email", columnDefinition = "VARCHAR(50)", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "nif", columnDefinition = "VARCHAR(20)", nullable = false, unique = true, length = 20)
    private String nif;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime createdAt;

    @Column(name = "last_modification", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.time.LocalDateTime lastModification;

    @OneToMany(mappedBy = "maintenanceProvider")
    @JsonIgnore
    private List<Equipment> equipments;
}