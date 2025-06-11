package com.labMetricas.LabMetricas.maintenance.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scheduled_maintenance")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledMaintenance {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "monthly_frequency", columnDefinition = "TINYINT UNSIGNED", nullable = false)
    private Short monthlyFrequency;

    @Column(name = "next_maintenance", nullable = false)
    private LocalDateTime nextMaintenance;

    @OneToOne
    @JoinColumn(name = "maintenance", nullable = false)
    private Maintenance maintenance;
} 