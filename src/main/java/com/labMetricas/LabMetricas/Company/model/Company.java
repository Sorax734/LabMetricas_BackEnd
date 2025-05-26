package com.labMetricas.LabMetricas.Company.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.user.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
// Company.java
@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "organization_name", columnDefinition = "VARCHAR(100)", nullable = false, length = 100)
    private String organizationName;

    @Column(name = "web_url", columnDefinition = "VARCHAR(255)")
    private String webUrl;

    @Column(name = "timezone", columnDefinition = "VARCHAR(50)", nullable = false)
    private String timezone = "UTC";

    @Column(name = "logo_url", columnDefinition = "VARCHAR(255)")
    private String logoUrl;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private List<User> users;
}