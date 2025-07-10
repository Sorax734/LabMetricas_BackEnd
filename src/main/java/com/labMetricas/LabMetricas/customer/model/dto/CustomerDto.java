package com.labMetricas.LabMetricas.customer.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto {
    private UUID id;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 50, message = "Email must be less than 50 characters")
    private String email;

    @NotBlank(message = "NIF is required")
    @Size(max = 20, message = "NIF must be less than 20 characters")
    private String nif;

    @Size(max = 255, message = "Address must be less than 255 characters")
    private String address;

    @Size(max = 20, message = "Phone must be less than 20 characters")
    private String phone;
} 