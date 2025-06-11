package com.labMetricas.LabMetricas.equipment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "status", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean status = true;

    @Column(name = "name", columnDefinition = "VARCHAR(50)", nullable = false, unique = true, length = 50)
    private String name;

    @OneToMany(mappedBy = "maintenanceProvider")
    @JsonIgnore
    private List<Equipment> equipments;
}