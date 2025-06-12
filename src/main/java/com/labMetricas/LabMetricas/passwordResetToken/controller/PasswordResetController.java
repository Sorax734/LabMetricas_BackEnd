package com.labMetricas.LabMetricas.passwordResetToken.controller;

import com.labMetricas.LabMetricas.security.PasswordResetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password-reset")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<String> requestPasswordReset(@RequestParam String email) {
        boolean result = passwordResetService.initiatePasswordReset(email);
        
        if (result) {
            return ResponseEntity.ok("Password reset link sent to your email");
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<Boolean> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.validateResetToken(token);
        return ResponseEntity.ok(isValid);
    }

    @PostMapping("/reset")
    public ResponseEntity<String> resetPassword(
        @RequestParam String token, 
        @RequestParam String newPassword
    ) {
        boolean result = passwordResetService.resetPassword(token, newPassword);
        
        if (result) {
            return ResponseEntity.ok("Password reset successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }
    }
} 