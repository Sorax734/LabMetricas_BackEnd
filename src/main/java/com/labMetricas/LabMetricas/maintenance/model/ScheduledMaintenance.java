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
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_type", nullable = false)
    private FrequencyType frequencyType;

    @Column(name = "frequency_value", columnDefinition = "SMALLINT", nullable = false)
    private Short frequencyValue;

    @Column(name = "next_maintenance", columnDefinition = "TIMESTAMP", nullable = false)
    private LocalDateTime nextMaintenance;

    @OneToOne
    @JoinColumn(name = "maintenance", nullable = false)
    private Maintenance maintenance;
} 