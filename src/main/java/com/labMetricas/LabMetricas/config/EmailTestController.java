package com.labMetricas.LabMetricas.config;

import com.labMetricas.LabMetricas.util.ResponseObject;
import com.labMetricas.LabMetricas.enums.TypeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*", maxAge = 3600)
public class EmailTestController {
    private static final Logger logger = LoggerFactory.getLogger(EmailTestController.class);

    @Autowired
    private EmailService emailService;

    @PostMapping("/test")
    public ResponseEntity<ResponseObject> testEmail(@RequestParam String email) {
        try {
            logger.info("Testing email service with email: {}", email);
            
            boolean emailSent = emailService.sendTestEmail(email);
            
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("sent", emailSent);
            data.put("sender", emailService.getDefaultSender());
            data.put("verifiedDomains", emailService.getVerifiedDomains());
            
            if (emailSent) {
                return ResponseEntity.ok(
                    new ResponseObject("Test email sent successfully", data, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject("Failed to send test email", data, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error testing email service", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("An error occurred while testing email service", null, TypeResponse.ERROR));
        }
    }

    @PostMapping("/test-custom-sender")
    public ResponseEntity<ResponseObject> testEmailWithCustomSender(
            @RequestParam String email, 
            @RequestParam String customSender) {
        try {
            logger.info("Testing email service with custom sender: {} to: {}", customSender, email);
            
            boolean emailSent = emailService.sendTestEmailWithCustomSender(email, customSender);
            
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("customSender", customSender);
            data.put("sent", emailSent);
            data.put("defaultSender", emailService.getDefaultSender());
            data.put("verifiedDomains", emailService.getVerifiedDomains());
            
            if (emailSent) {
                return ResponseEntity.ok(
                    new ResponseObject("Test email sent successfully with custom sender", data, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject("Failed to send test email with custom sender", data, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error testing email service with custom sender", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("An error occurred while testing email service with custom sender", null, TypeResponse.ERROR));
        }
    }

    @PostMapping("/test-any-domain")
    public ResponseEntity<ResponseObject> testEmailAnyDomain(@RequestParam String email) {
        try {
            logger.info("Testing email service with any domain email: {}", email);
            
            // Para testing, permitimos cualquier dominio
            boolean emailSent = emailService.sendTestEmail(email);
            
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("sent", emailSent);
            data.put("sender", emailService.getDefaultSender());
            data.put("note", "This endpoint allows testing with any email domain for development purposes");
            
            if (emailSent) {
                return ResponseEntity.ok(
                    new ResponseObject("Test email sent successfully to any domain", data, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject("Failed to send test email to any domain", data, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error testing email service with any domain", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("An error occurred while testing email service with any domain", null, TypeResponse.ERROR));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<ResponseObject> getEmailServiceStatus() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("service", "Email Service");
            data.put("provider", "Resend");
            data.put("status", "Available");
            data.put("defaultSender", emailService.getDefaultSender());
            data.put("verifiedDomains", emailService.getVerifiedDomains());
            data.put("timestamp", java.time.LocalDateTime.now());
            
            return ResponseEntity.ok(
                new ResponseObject("Email service status", data, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error getting email service status", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("Error getting email service status", null, TypeResponse.ERROR));
        }
    }

    @GetMapping("/config")
    public ResponseEntity<ResponseObject> getEmailServiceConfig() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("defaultSender", emailService.getDefaultSender());
            data.put("verifiedDomains", emailService.getVerifiedDomains());
            data.put("apiKeyConfigured", emailService.getDefaultSender() != null && !emailService.getDefaultSender().isEmpty());
            
            return ResponseEntity.ok(
                new ResponseObject("Email service configuration", data, TypeResponse.SUCCESS)
            );
        } catch (Exception e) {
            logger.error("Error getting email service configuration", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("Error getting email service configuration", null, TypeResponse.ERROR));
        }
    }
}
