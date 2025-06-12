package com.labMetricas.LabMetricas.security;

import com.labMetricas.LabMetricas.passwordResetToken.model.PasswordResetToken;
import com.labMetricas.LabMetricas.passwordResetToken.repository.PasswordResetTokenRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.model.UserRepository;
import com.resend.Resend;
import com.resend.services.emails.model.SendEmailRequest;
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
    private Resend resend;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${resend.default.sender}")
    private String defaultSender;

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
            SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .from(defaultSender)
                .to(email)
                .subject("Password Reset Token")
                .html(buildResetEmailBody(token))
                .build();

            try {
                resend.emails().send(sendEmailRequest);
                logger.info("Password reset token sent to: {}", email);
                return true;
            } catch (Exception emailEx) {
                logger.error("Failed to send email via Resend", emailEx);
                return false;
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
                SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                    .from(defaultSender)
                    .to(user.getEmail())
                    .subject("Password Successfully Reset")
                    .html(buildPasswordResetConfirmationEmailBody(user.getName()))
                    .build();

                try {
                    resend.emails().send(sendEmailRequest);
                    logger.info("Password reset confirmation email sent to: {}", user.getEmail());
                } catch (Exception emailEx) {
                    logger.error("Failed to send confirmation email via Resend", emailEx);
                }
            }
        } catch (Exception e) {
            logger.error("Unexpected error in sending password reset confirmation email", e);
        }
    }

    private String buildResetEmailBody(String token) {
        return String.format(
            "<html><body>" +
            "<h2>Password Reset Request</h2>" +
            "<p>We received a request to reset your password. Here is your password reset token:</p>" +
            "<div style='background-color: #f4f4f4; padding: 15px; border-radius: 5px; font-family: monospace; text-align: center; font-size: 18px;'>" +
            "%s" +
            "</div>" +
            "<p>This token will expire in 1 hour. If you did not request a password reset, please ignore this email.</p>" +
            "<p>Do not share this token with anyone.</p>" +
            "</body></html>", 
            token
        );
    }

    private String buildPasswordResetConfirmationEmailBody(String userName) {
        return String.format(
            "<html><body>" +
            "<h2>Password Reset Confirmation</h2>" +
            "<p>Hello %s,</p>" +
            "<p>Your password has been successfully reset. If you did not make this change, please contact our support team immediately.</p>" +
            "<p>If this was you, no further action is required.</p>" +
            "</body></html>", 
            userName
        );
    }
} 