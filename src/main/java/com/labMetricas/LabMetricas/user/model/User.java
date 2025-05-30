package com.labMetricas.LabMetricas.user.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.labMetricas.LabMetricas.Company.model.Company;
import com.labMetricas.LabMetricas.Notice.model.Notice;
import com.labMetricas.LabMetricas.NoticeRecipient.model.NoticeRecipient;
import com.labMetricas.LabMetricas.document.model.Document;
import com.labMetricas.LabMetricas.passwordResetToken.model.PasswordResetToken;
import com.labMetricas.LabMetricas.role.model.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
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
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "name", columnDefinition = "VARCHAR(50)", nullable = false, length = 50)
    private String name;

    @Column(name = "lastname", columnDefinition = "VARCHAR(50)", nullable = false, length = 50)
    private String lastname;

    @Column(name = "email", columnDefinition = "VARCHAR(50)", nullable = false, unique = true, length = 50)
    private String email;

    @Column(name = "password", columnDefinition = "VARCHAR(100)", nullable = false, length = 100)
    private String password;

    @Column(name = "phone", columnDefinition = "VARCHAR(10)", length = 10)
    private String phone;

    @Column(name = "birth_date", columnDefinition = "DATE")
    private LocalDate birthDate;

    @Column(name = "company_name", columnDefinition = "VARCHAR(40)", length = 40)
    private String companyName;

    @Column(name = "residence", columnDefinition = "TINYTEXT")
    private String residence;

    @Column(name = "status", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean status = true;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "created_at", updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
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