package com.labMetricas.LabMetricas.document.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.documentVersion.model.DocumentVersion;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.List;


@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", columnDefinition = "VARCHAR(255)", nullable = false)
    private String name;

    @Column(name = "url", columnDefinition = "VARCHAR(255)", nullable = false)
    private String url;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean status = true;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;  // Esta es la referencia correcta

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "document")
    @JsonIgnore
    private List<DocumentVersion> versions;

    @OneToMany(mappedBy = "document")
    @JsonIgnore
    private List<AuditLog> auditLogs;
}