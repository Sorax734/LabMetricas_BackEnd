package com.labMetricas.LabMetricas.customer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers", 
    indexes = {
        @Index(name = "customer_name_index", columnList = "name"),
        @Index(name = "customer_email_index", columnList = "email"),
        @Index(name = "customer_nif_index", columnList = "nif")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", columnDefinition = "VARCHAR(100)", nullable = false, length = 100)
    private String name;

    @Column(name = "address", columnDefinition = "VARCHAR(255)", nullable = true, length = 255)
    private String address;

    @Column(name = "phone", columnDefinition = "VARCHAR(20)", nullable = true, length = 20)
    private String phone;

    @Column(name = "status", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean status = true;

    @Column(name = "email", columnDefinition = "VARCHAR(50)", nullable = false, unique = true, length = 50)
    private String email;


    @Column(name = "nif", columnDefinition = "VARCHAR(20)", nullable = false, unique = true, length = 20)
    private String nif;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "last_modification", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime lastModification;
} 