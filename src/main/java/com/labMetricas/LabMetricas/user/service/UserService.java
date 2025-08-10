package com.labMetricas.LabMetricas.user.service;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.role.repository.RoleRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.user.model.dto.ChangePasswordDto;
import com.labMetricas.LabMetricas.user.model.dto.UserDto;
import com.labMetricas.LabMetricas.util.ResponseObject;
import com.resend.Resend;
import com.resend.services.emails.model.SendEmailRequest;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Resend resend;

    @Value("${resend.default.sender}")
    private String defaultSender;

    @Value("${frontend.url}")
    private String frontendUrl;

    // Method to generate a secure password
    private String generateSecurePassword() {
        // Generate a password with at least 8 characters, including:
        // - An uppercase letter
        // - A special character
        // - Remaining characters can be mixed
        String uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String specialCharacters = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String allCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" + specialCharacters;

        StringBuilder password = new StringBuilder();

        // Add one uppercase letter
        password.append(uppercaseLetters.charAt((int) (Math.random() * uppercaseLetters.length())));

        // Add one special character
        password.append(specialCharacters.charAt((int) (Math.random() * specialCharacters.length())));

        // Fill the rest of the password to make it at least 8 characters long
        while (password.length() < 8) {
            password.append(allCharacters.charAt((int) (Math.random() * allCharacters.length())));
        }

        return password.toString();
    }

    // Method to send welcome email with temporary password
    private void sendWelcomeEmail(String email, String temporaryPassword, String name) {
        try {
            // Create a stylish, informative welcome email
            SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .from(defaultSender)
                .to(email)
                .subject("Bienvenido a LabMetricas - Tus Credenciales de Acceso")
                .html(buildWelcomeEmailBody(name, email, temporaryPassword))
                .build();

            resend.emails().send(sendEmailRequest);
            logger.info("Welcome email sent to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send welcome email to: {}", email, e);
        }
    }

    // Helper method to build a stylish welcome email body
    private String buildWelcomeEmailBody(String name, String email, String temporaryPassword) {
        return String.format(
            "<html>" +
            "<body style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background-color: #f4f4f4;'>" +
            "    <div style='background-color: #ffffff; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); padding: 30px;'>" +
            "        <h1 style='color: #2c3e50; text-align: center;'>¡Bienvenido a LabMetricas!</h1>" +
            "        <p style='color: #34495e; line-height: 1.6;'>Estimado/a <strong>%s</strong>,</p>" +
            "        <div style='background-color: #ecf0f1; border-left: 5px solid #3498db; padding: 15px; margin: 20px 0;'>" +
            "            <h2 style='color: #2980b9; margin-top: 0;'>Tus Claves de Acceso</h2>" +
            "            <p style='margin: 10px 0;'><strong>Correo Electrónico:</strong> <span style='color: #2c3e50;'>%s</span></p>" +
            "            <p style='margin: 10px 0;'><strong>Contraseña Temporal:</strong> <span style='color: #e74c3c; font-family: monospace;'>%s</span></p>" +
            "        </div>" +
            "        <p style='color: #34495e; line-height: 1.6;'>Por razones de seguridad, te recomendamos cambiar tu contraseña después de tu primer inicio de sesión.</p>" +
            "        <div style='text-align: center; margin-top: 30px;'>" +
            "            <a href='%s' style='background-color: #3498db; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Iniciar Sesión</a>" +
            "        </div>" +
            "        <p style='color: #7f8c8d; font-size: 0.9em; text-align: center; margin-top: 20px;'>Si no solicitaste esta cuenta, por favor contacta con soporte.</p>" +
            "    </div>" +
            "</body>" +
            "</html>", 
            name, email, temporaryPassword, frontendUrl
        );
    }

    @Transactional
    public ResponseEntity<ResponseObject> createUser(UserDto userDto) {
        try {
            // Check if email already exists
            if (userRepository.existsByEmail(userDto.getEmail())) {
                return ResponseEntity.badRequest().body(
                    new ResponseObject("Email already exists", null, TypeResponse.ERROR)
                );
            }

            // Create new user
            User user = new User();
            user.setName(userDto.getName());
            user.setEmail(userDto.getEmail());
            user.setPosition(userDto.getPosition());
            user.setPhone(userDto.getPhone());
            user.setStatus(userDto.getStatus());
            
            // Set role
            user.setRole(roleRepository.findById(userDto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found")));

            // Generate or use provided password
            String rawPassword = userDto.getPassword() != null && !userDto.getPassword().isEmpty() 
                ? userDto.getPassword() 
                : generateSecurePassword();

            // Encode password
            user.setPassword(passwordEncoder.encode(rawPassword));
            
            // Set timestamps
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // Save user
            User savedUser = userRepository.save(user);

            // Convert to DTO for response
            UserDto responseDto = convertToDto(savedUser);

            // Always set the temporary password in the response
            responseDto.setTemporaryPassword(rawPassword);

            // Send welcome email with temporary password
            sendWelcomeEmail(savedUser.getEmail(), rawPassword, savedUser.getName());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                new ResponseObject("User created successfully", responseDto, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error creating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error creating user", null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> updateUser(UserDto userDto) {
        try {
            // Find existing user
            User existingUser = userRepository.findById(userDto.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Update user details
            existingUser.setName(userDto.getName());
            existingUser.setPosition(userDto.getPosition());
            existingUser.setPhone(userDto.getPhone());
            existingUser.setStatus(userDto.getStatus());

            Optional<User> user = userRepository.findByEmail(userDto.getEmail());
            if (user.isPresent() && !user.get().getId().equals(existingUser.getId())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        new ResponseObject("Email already exists", TypeResponse.ERROR));
            } else {
                existingUser.setEmail(userDto.getEmail());
            }

            // Update role if changed
            if (!existingUser.getRole().getId().equals(userDto.getRoleId())) {
                existingUser.setRole(roleRepository.findById(userDto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found")));
            }

            // Update timestamps
            existingUser.setUpdatedAt(LocalDateTime.now());

            // Save updated user
            User updatedUser = userRepository.save(existingUser);

            // Convert to DTO for response
            UserDto responseDto = convertToDto(updatedUser);

            return ResponseEntity.ok(
                new ResponseObject("User updated successfully", responseDto, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error updating user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error updating user", null, TypeResponse.ERROR)
            );
        }
    }

    public ResponseEntity<ResponseObject> getUserById(UUID id) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(
                new ResponseObject("User retrieved successfully", convertToDto(user), TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving user", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("User not found", null, TypeResponse.ERROR)
            );
        }
    }

    public ResponseEntity<ResponseObject> getAllUsers() {
        try {
            List<UserDto> users = userRepository.findAllWithRoles().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

            return ResponseEntity.ok(
                new ResponseObject("Users retrieved successfully", users, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error retrieving users", null, TypeResponse.ERROR)
            );
        }
    }

    public ResponseEntity<ResponseObject> getAllUsersButMe(String userEmail) {
        try {
            String normalized = userEmail.trim().toLowerCase();

            List<UserDto> users = userRepository.findAllWithRoles().stream()
                    .filter(u -> u.getEmail() == null || !normalized.equals(u.getEmail().trim().toLowerCase()))
                    .map(this::convertToDto)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(
                    new ResponseObject("Users retrieved successfully (excluding current)", users, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ResponseObject("Error retrieving users", null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> deleteUser(UUID id) {
        try {
            User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Soft delete
            user.setStatus(false);
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);

            return ResponseEntity.ok(
                new ResponseObject("User deleted successfully", null, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error deleting user", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error deleting user", null, TypeResponse.ERROR)
            );
        }
    }

    @Transactional
    public ResponseEntity<ResponseObject> changePassword(ChangePasswordDto dto) {
        try {
            // Get current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            // Find user by email
            User user = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate current password
            if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.badRequest().body(
                    new ResponseObject("Current password is incorrect", null, TypeResponse.ERROR)
                );
            }

            // Encode and set new password
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);

            return ResponseEntity.ok(
                new ResponseObject("Password changed successfully", null, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error changing password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error changing password", null, TypeResponse.ERROR)
            );
        }
    }

    // Method to get user by email
    public ResponseEntity<ResponseObject> getUserByEmail(String email) {
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            return ResponseEntity.ok(
                new ResponseObject("User retrieved successfully", convertToDto(user), TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error retrieving user by email", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ResponseObject("User not found", null, TypeResponse.ERROR)
            );
        }
    }

    // Method to delete user by email
    @Transactional
    public ResponseEntity<ResponseObject> deleteUserByEmail(String email) {
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

            // Soft delete
            user.setStatus(!user.getStatus());
            user.setDeletedAt(LocalDateTime.now());
            userRepository.saveAndFlush(user);

            return ResponseEntity.ok(
                new ResponseObject("User deleted successfully", null, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error deleting user by email", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ResponseObject("Error deleting user", null, TypeResponse.ERROR)
            );
        }
    }

    // Method to find user by email
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    // Helper method to convert User to UserDto
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPosition(user.getPosition());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRoleId(user.getRole() != null ? user.getRole().getId() : null);
        return dto;









    }
} 