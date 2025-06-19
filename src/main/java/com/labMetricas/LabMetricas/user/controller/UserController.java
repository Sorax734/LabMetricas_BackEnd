package com.labMetricas.LabMetricas.user.controller;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.user.dto.UserDetailsDto;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.model.dto.ChangePasswordDto;
import com.labMetricas.LabMetricas.user.model.dto.UserDto;
import com.labMetricas.LabMetricas.user.service.UserService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    // Constructor injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Profile endpoint for all authenticated users
    @GetMapping("/profile")
    public ResponseEntity<ResponseObject> getUserProfile() {
        try {
            // Get current authentication
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            
            // Log the profile request
            logger.info("Profile request for user: {}", auth.getName());
            
            // Retrieve and convert user details
            User currentUser = userService.findByEmail(auth.getName());
            UserDetailsDto userProfile = new UserDetailsDto(currentUser);
            
            // Return successful response
            return ResponseEntity.ok(
                new ResponseObject("Profile details retrieved successfully", userProfile, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            // Log and handle any errors
            logger.error("Error retrieving profile: {}", e.getMessage());
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ResponseObject("Error retrieving profile details", null, TypeResponse.ERROR));
        }
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> createUser(@Valid @RequestBody UserDto userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to create a new user", auth.getName());
        
        return userService.createUser(userDto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> updateUser(@Valid @RequestBody UserDto userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to update user with email {}", auth.getName(), userDto.getEmail());
        
        return userService.updateUser(userDto);
    }

    @GetMapping("/{email}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> getUserByEmail(@PathVariable String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve user with email {}", auth.getName(), email);
        
        return userService.getUserByEmail(email);
    }

    @PostMapping("/find-by-email")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> findUserByEmail(@RequestBody Map<String, String> payload) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Extract email from payload
        String email = payload.get("email");
        
        // Log the attempt
        logger.info("User {} attempting to find user with email {}", auth.getName(), email);
        
        // Validate email
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body(
                new ResponseObject("Email is required", null, TypeResponse.ERROR)
            );
        }
        
        // Delegate to service method
        return userService.getUserByEmail(email);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve all users", auth.getName());
        
        return userService.getAllUsers();
    }

    @DeleteMapping("/{email}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseObject> deleteUser(@PathVariable String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to delete user with email {}", auth.getName(), email);
        
        return userService.deleteUserByEmail(email);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ResponseObject> changePassword(@Valid @RequestBody ChangePasswordDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to change password", auth.getName());
        
        return userService.changePassword(dto);
    }

    // Global exception handler for unauthorized access
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseObject> handleAccessDeniedException(AccessDeniedException ex) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log unauthorized access attempt
        logger.warn("Unauthorized access attempt by user {} with authorities {}", 
            auth.getName(), auth.getAuthorities());
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            new ResponseObject("Access denied. Insufficient permissions.", null, TypeResponse.ERROR)
        );
    }
} 