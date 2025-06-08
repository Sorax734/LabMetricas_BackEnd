package com.labMetricas.LabMetricas.maintenance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "maintenance_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_maintenance_type")
    private Integer id;

    @Column(name = "status", nullable = false)
    private Boolean status = true;

    @Column(name = "name", nullable = false, length = 45)
    private String name;

    @OneToMany(mappedBy = "maintenanceType")
    @JsonIgnore
    private List<Maintenance> maintenances;
} 