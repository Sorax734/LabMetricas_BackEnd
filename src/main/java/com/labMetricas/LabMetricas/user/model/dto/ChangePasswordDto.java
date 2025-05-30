package com.labMetricas.LabMetricas.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;

    @NotBlank(message = "Email is required")
    private String email;
} 