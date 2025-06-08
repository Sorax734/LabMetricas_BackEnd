package com.labMetricas.LabMetricas.maintenance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_maintenance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledMaintenance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_scheduled_maintenance")
    private Integer id;

    @Column(name = "frecuency", nullable = false, length = 2)
    private String frequency;

    @Column(name = "next_maintenance", nullable = false)
    private LocalDateTime nextMaintenance;

    @OneToOne
    @JoinColumn(name = "maintenance", nullable = false)
    private Maintenance maintenance;
} 