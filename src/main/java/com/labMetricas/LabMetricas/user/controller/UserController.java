package com.labMetricas.LabMetricas.user.controller;

import com.labMetricas.LabMetricas.enums.TypeResponse;
import com.labMetricas.LabMetricas.security.AuthController;
import com.labMetricas.LabMetricas.user.dto.UserDetailsDto;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.model.dto.ChangePasswordDto;
import com.labMetricas.LabMetricas.user.model.dto.UserDto;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.user.service.UserService;
import com.labMetricas.LabMetricas.util.ResponseObject;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    private UserRepository userRepository;

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
    public ResponseEntity<ResponseObject> createUser(@Valid @RequestBody UserDto userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to create a new user", auth.getName());
        
        return userService.createUser(userDto);
    }

    @PutMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResponseObject> updateUserProfile(@Valid @RequestBody UserDto userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = auth.getName();

        logger.info("User {} attempting to update own profile", currentEmail);

        Optional<User> user = userRepository.findByEmail(currentEmail);

        // Verificamos que el id del DTO coincida con el del usuario autenticado
        if (!user.get().getId().equals(userDto.getId())) {
            logger.warn("User id mismatch: principal={} vs dto={}", user.get().getId(), userDto.getId());

            return ResponseEntity.badRequest().body(
                    new ResponseObject("No tienes permiso para actualizar este perfil", null, TypeResponse.ERROR)
            );
        }

        return userService.updateUser(userDto);
    }

    @PutMapping
    public ResponseEntity<ResponseObject> updateUser(@Valid @RequestBody UserDto userDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to update user with email {}", auth.getName(), userDto.getEmail());
        
        return userService.updateUser(userDto);
    }

    @GetMapping("/{email}")
    public ResponseEntity<ResponseObject> getUserByEmail(@PathVariable String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve user with email {}", auth.getName(), email);
        
        return userService.getUserByEmail(email);
    }

    @PostMapping("/find-by-email")
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
    public ResponseEntity<ResponseObject> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Log the attempt
        logger.info("User {} attempting to retrieve all users", auth.getName());
        
        return userService.getAllUsers();
    }

    @GetMapping("/ButMe")
    public ResponseEntity<ResponseObject> getAllUsersButMe() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // Log the attempt
        logger.info("User {} attempting to retrieve all users excluding self", auth.getName());

        return userService.getAllUsersButMe(auth.getName());
    }

    @DeleteMapping("/{email}")
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