package com.labMetricas.LabMetricas.EquipmentCategory.model;

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
@Table(name = "equipment_category", 
    indexes = {
        @Index(name = "equipment_category_name_index", columnList = "name"),
        @Index(name = "equipment_category_status_index", columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "status", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean status = true;

    @Column(name = "name", columnDefinition = "VARCHAR(100)", nullable = false, unique = true, length = 100)
    private String name;

    @OneToMany(mappedBy = "equipmentCategory")
    @JsonIgnore
    private List<Equipment> equipments;
} 