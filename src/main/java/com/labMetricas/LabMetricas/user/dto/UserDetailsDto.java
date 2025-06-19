package com.labMetricas.LabMetricas.user.dto;

import com.labMetricas.LabMetricas.user.model.User;
import java.io.Serializable;
import java.util.UUID;


public class UserDetailsDto implements Serializable {
    private UUID id;
    private String name;
    private String email;
    private String position;
    private String phone;
    private Boolean status;
    private String roleName;

    public UserDetailsDto() {}

    public UserDetailsDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.position = user.getPosition();
        this.phone = user.getPhone();
        this.status = user.getStatus();
        this.roleName = user.getRole() != null ? user.getRole().getName() : null;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
} 