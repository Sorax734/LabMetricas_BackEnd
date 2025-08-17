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
@RequestMapping("/api/production/email")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductionEmailController {
    private static final Logger logger = LoggerFactory.getLogger(ProductionEmailController.class);

    @Autowired
    private ProductionEmailService productionEmailService;

    /**
     * Enviar email de bienvenida
     */
    @PostMapping("/welcome")
    public ResponseEntity<ResponseObject> sendWelcomeEmail(
            @RequestParam String email, 
            @RequestParam String userName) {
        try {
            logger.info("Sending welcome email to: {} for user: {}", email, userName);
            
            boolean emailSent = productionEmailService.sendWelcomeEmail(email, userName);
            
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("userName", userName);
            data.put("sent", emailSent);
            data.put("type", "welcome");
            
            if (emailSent) {
                return ResponseEntity.ok(
                    new ResponseObject("Welcome email sent successfully", data, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject("Failed to send welcome email", data, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error sending welcome email", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("An error occurred while sending welcome email", null, TypeResponse.ERROR));
        }
    }

    /**
     * Enviar notificación de mantenimiento
     */
    @PostMapping("/maintenance-notification")
    public ResponseEntity<ResponseObject> sendMaintenanceNotification(
            @RequestParam String email, 
            @RequestParam String userName,
            @RequestParam String maintenanceType,
            @RequestParam String scheduledDate) {
        try {
            logger.info("Sending maintenance notification to: {} for user: {}", email, userName);
            
            boolean emailSent = productionEmailService.sendMaintenanceNotification(email, userName, maintenanceType, scheduledDate);
            
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("userName", userName);
            data.put("maintenanceType", maintenanceType);
            data.put("scheduledDate", scheduledDate);
            data.put("sent", emailSent);
            data.put("type", "maintenance_notification");
            
            if (emailSent) {
                return ResponseEntity.ok(
                    new ResponseObject("Maintenance notification sent successfully", data, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject("Failed to send maintenance notification", data, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error sending maintenance notification", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("An error occurred while sending maintenance notification", null, TypeResponse.ERROR));
        }
    }

    /**
     * Enviar recordatorio de contraseña
     */
    @PostMapping("/password-reminder")
    public ResponseEntity<ResponseObject> sendPasswordReminder(
            @RequestParam String email, 
            @RequestParam String userName) {
        try {
            logger.info("Sending password reminder to: {} for user: {}", email, userName);
            
            boolean emailSent = productionEmailService.sendPasswordReminder(email, userName);
            
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("userName", userName);
            data.put("sent", emailSent);
            data.put("type", "password_reminder");
            
            if (emailSent) {
                return ResponseEntity.ok(
                    new ResponseObject("Password reminder sent successfully", data, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject("Failed to send password reminder", data, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error sending password reminder", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("An error occurred while sending password reminder", null, TypeResponse.ERROR));
        }
    }

    /**
     * Enviar confirmación de acción
     */
    @PostMapping("/action-confirmation")
    public ResponseEntity<ResponseObject> sendActionConfirmation(
            @RequestParam String email, 
            @RequestParam String userName,
            @RequestParam String action,
            @RequestParam String details) {
        try {
            logger.info("Sending action confirmation to: {} for user: {}", email, userName);
            
            boolean emailSent = productionEmailService.sendActionConfirmation(email, userName, action, details);
            
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("userName", userName);
            data.put("action", action);
            data.put("details", details);
            data.put("sent", emailSent);
            data.put("type", "action_confirmation");
            
            if (emailSent) {
                return ResponseEntity.ok(
                    new ResponseObject("Action confirmation sent successfully", data, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject("Failed to send action confirmation", data, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error sending action confirmation", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("An error occurred while sending action confirmation", null, TypeResponse.ERROR));
        }
    }

    /**
     * Enviar email personalizado
     */
    @PostMapping("/custom")
    public ResponseEntity<ResponseObject> sendCustomEmail(
            @RequestParam String email, 
            @RequestParam String subject,
            @RequestParam String htmlContent) {
        try {
            logger.info("Sending custom email to: {} with subject: {}", email, subject);
            
            boolean emailSent = productionEmailService.sendCustomEmail(email, subject, htmlContent);
            
            Map<String, Object> data = new HashMap<>();
            data.put("email", email);
            data.put("subject", subject);
            data.put("sent", emailSent);
            data.put("type", "custom");
            
            if (emailSent) {
                return ResponseEntity.ok(
                    new ResponseObject("Custom email sent successfully", data, TypeResponse.SUCCESS)
                );
            } else {
                return ResponseEntity.badRequest()
                    .body(new ResponseObject("Failed to send custom email", data, TypeResponse.ERROR));
            }
        } catch (Exception e) {
            logger.error("Error sending custom email", e);
            return ResponseEntity.internalServerError()
                .body(new ResponseObject("An error occurred while sending custom email", null, TypeResponse.ERROR));
        }
    }
}
