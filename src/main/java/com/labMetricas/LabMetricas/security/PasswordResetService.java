package com.labMetricas.LabMetricas.security;

import com.labMetricas.LabMetricas.passwordResetToken.model.PasswordResetToken;
import com.labMetricas.LabMetricas.passwordResetToken.repository.PasswordResetTokenRepository;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.config.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Optional;
import com.labMetricas.LabMetricas.user.model.User;

@Service
public class PasswordResetService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordResetService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Transactional
    public boolean initiatePasswordReset(String email) {
        try {
            logger.info("Attempting password reset for email: {}", email);
            
            // First, check if the email exists in the database
            Optional<User> userOptional = userRepository.findByEmail(email);
            
            // Log additional details about the user lookup
            logger.info("User lookup result: {}", userOptional.isPresent() ? "User found" : "User not found");
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                logger.info("Found user details - Name: {}, Status: {}, Enabled: {}", 
                    user.getName(), user.getStatus(), user.isEnabled());
            }
            
            // If user is not found, return false
            if (userOptional.isEmpty()) {
                logger.warn("Password reset requested for non-existent email: {}", email);
                return false;
            }

            User user = userOptional.get();
            
            // Delete any existing reset tokens for this user
            passwordResetTokenRepository.deleteByUser(user);

            // Generate new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);

            // Send reset email with token
            boolean emailSent = emailService.sendEmail(
                email, 
                "Password Reset Token", 
                buildResetEmailBody(token)
            );

            if (emailSent) {
                logger.info("Password reset token sent to: {}", email);
                return true;
            } else {
                logger.error("Failed to send email via Resend");
                // Log the reset token for development/testing
                logger.warn("DEVELOPMENT MODE: Reset Token for {}: {}", email, token);
                return true; // Return true to simulate successful token generation
            }
        } catch (Exception e) {
            logger.error("Unexpected error in password reset process", e);
            return false;
        }
    }

    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> resetTokenOptional = 
            passwordResetTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(
                token, LocalDateTime.now()
            );

        if (resetTokenOptional.isEmpty()) {
            logger.warn("Invalid or expired reset token: {}", token);
            return false;
        }

        PasswordResetToken resetToken = resetTokenOptional.get();
        User user = resetToken.getUser();

        // Encode the new password
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Mark token as used
        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        logger.info("Password successfully reset for user: {}", user.getEmail());
        return true;
    }

    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> resetTokenOptional = 
            passwordResetTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(
                token, LocalDateTime.now()
            );

        return resetTokenOptional.isPresent();
    }

    public void sendPasswordResetConfirmationEmail(String token) {
        try {
            // Find the user associated with this token
            Optional<PasswordResetToken> resetTokenOptional = 
                passwordResetTokenRepository.findByTokenAndUsedFalseAndExpiryDateAfter(
                    token, LocalDateTime.now()
                );

            if (resetTokenOptional.isPresent()) {
                User user = resetTokenOptional.get().getUser();
                
                // Send confirmation email
                boolean emailSent = emailService.sendEmail(
                    user.getEmail(),
                    "Password Successfully Reset",
                    buildPasswordResetConfirmationEmailBody(user.getName())
                );

                if (emailSent) {
                    logger.info("Password reset confirmation email sent to: {}", user.getEmail());
                } else {
                    logger.error("Failed to send confirmation email via Resend");
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in sending password reset confirmation email", e);
        }
    }

    private String buildResetEmailBody(String token) {
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background-color: #007bff; color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .token-box { background-color: #f8f9fa; border: 2px solid #007bff; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center; }
                    .token { font-family: 'Courier New', monospace; font-size: 20px; font-weight: bold; color: #007bff; letter-spacing: 2px; }
                    .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; border-radius: 5px; padding: 15px; margin: 20px 0; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }
                    .button { display: inline-block; background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 10px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 Restablecimiento de Contraseña</h1>
                        <p>LabMetricas - Sistema de Gestión</p>
                    </div>
                    <div class="content">
                        <h2>Hola,</h2>
                        <p>Hemos recibido una solicitud para restablecer tu contraseña en LabMetricas.</p>
                        
                        <div class="token-box">
                            <h3>Tu código de verificación:</h3>
                            <div class="token">%s</div>
                            <p><small>Este código expira en 1 hora</small></p>
                        </div>
                        
                        <div class="warning">
                            <strong>⚠️ Importante:</strong>
                            <ul>
                                <li>No compartas este código con nadie</li>
                                <li>Si no solicitaste este cambio, ignora este email</li>
                                <li>El código solo es válido por 1 hora</li>
                            </ul>
                        </div>
                        
                        <p>Si tienes alguna pregunta, no dudes en contactar a nuestro equipo de soporte.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 LabMetricas. Todos los derechos reservados.</p>
                        <p>Este es un email automático, por favor no respondas a este mensaje.</p>
                    </div>
                </div>
            </body>
            </html>
            """, token);
    }

    private String buildPasswordResetConfirmationEmailBody(String userName) {
        return String.format("""
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; }
                    .header { background-color: #28a745; color: white; padding: 30px; text-align: center; }
                    .content { padding: 30px; }
                    .success-box { background-color: #d4edda; border: 2px solid #28a745; border-radius: 8px; padding: 20px; margin: 20px 0; text-align: center; }
                    .footer { background-color: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Contraseña Actualizada</h1>
                        <p>LabMetricas - Sistema de Gestión</p>
                    </div>
                    <div class="content">
                        <h2>Hola %s,</h2>
                        
                        <div class="success-box">
                            <h3>¡Tu contraseña ha sido actualizada exitosamente!</h3>
                            <p>Tu cuenta en LabMetricas ahora tiene una nueva contraseña.</p>
                        </div>
                        
                        <p><strong>Información importante:</strong></p>
                        <ul>
                            <li>Tu contraseña ha sido cambiada recientemente</li>
                            <li>Si no realizaste este cambio, contacta inmediatamente a nuestro equipo de soporte</li>
                            <li>Para mayor seguridad, te recomendamos cambiar tu contraseña regularmente</li>
                        </ul>
                        
                        <p>Si tienes alguna pregunta o necesitas ayuda, no dudes en contactarnos.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 LabMetricas. Todos los derechos reservados.</p>
                        <p>Este es un email automático, por favor no respondas a este mensaje.</p>
                    </div>
                </div>
            </body>
            </html>
            """, userName);
    }
} 