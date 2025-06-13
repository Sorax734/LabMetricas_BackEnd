package com.labMetricas.LabMetricas.user.service;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.role.repository.RoleRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.user.model.dto.ChangePasswordDto;
import com.labMetricas.LabMetricas.user.model.dto.UserDto;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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

            // Encode password
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            
            // Set timestamps
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // Save user
            User savedUser = userRepository.save(user);

            // Convert to DTO for response
            UserDto responseDto = convertToDto(savedUser);

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
            user.setStatus(false);
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);

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

    // Helper method to convert User to UserDto
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPosition(user.getPosition());
        dto.setPhone(user.getPhone());
        dto.setStatus(user.getStatus());
        dto.setRoleId(user.getRole() != null ? user.getRole().getId() : null);
        return dto;
    }
} 