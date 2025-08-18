package com.labMetricas.LabMetricas.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.role.model.Role;
import com.labMetricas.LabMetricas.Notice.model.Notice;
import com.labMetricas.LabMetricas.NoticeRecipient.model.NoticeRecipient;
import com.labMetricas.LabMetricas.document.model.Document;
import com.labMetricas.LabMetricas.passwordResetToken.model.PasswordResetToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users", 
    indexes = {
        @Index(name = "user_name_index", columnList = "name"),
        @Index(name = "user_email_index", columnList = "email"),
        @Index(name = "user_phone_index", columnList = "phone"),
        @Index(name = "user_status_index", columnList = "status")
    })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "name", columnDefinition = "VARCHAR(50)", nullable = false, length = 50)
    private String name;

    @Column(name = "email", columnDefinition = "VARCHAR(50)", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "password", columnDefinition = "VARCHAR(100)", nullable = false, length = 100)
    @JsonIgnore
    private String password;

    @Column(name = "position", columnDefinition = "VARCHAR(50)", nullable = false, length = 50)
    private String position;

    @Column(name = "phone", columnDefinition = "VARCHAR(10)", length = 10)
    private String phone;

    @Column(name = "status", columnDefinition = "BOOLEAN", nullable = false)
    private Boolean status = true;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "assignedTo")
    @JsonIgnore
    private List<Equipment> equipments;

    @OneToMany(mappedBy = "responsible")
    @JsonIgnore
    private List<Maintenance> maintenances;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Document> documents;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<PasswordResetToken> passwordResetTokens;

    @OneToMany(mappedBy = "createdBy")
    @JsonIgnore
    private List<Notice> createdNotices;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<NoticeRecipient> receivedNotices;

    private boolean enabled = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.getName()));
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled && this.status;
    }
}