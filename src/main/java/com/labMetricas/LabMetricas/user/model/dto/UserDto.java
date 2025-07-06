package com.labMetricas.LabMetricas.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be less than 50 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must be less than 50 characters")
    private String email;

    @NotBlank(message = "Position is required")
    @Size(max = 50, message = "Position must be less than 50 characters")
    private String position;

    @Size(max = 10, message = "Phone must be less than 10 characters")
    private String phone;

    private Boolean status = true;

    private Long roleId;

    private String password;

    private String temporaryPassword;

    private LocalDateTime updatedAt;
} 