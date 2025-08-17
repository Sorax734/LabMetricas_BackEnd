package com.labMetricas.LabMetricas.config;

import com.resend.Resend;
import com.resend.services.emails.model.SendEmailRequest;
import com.resend.services.emails.model.SendEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProductionEmailService {
    private static final Logger logger = LoggerFactory.getLogger(ProductionEmailService.class);

    @Autowired
    private Resend resend;

    @Value("${resend.default.sender}")
    private String defaultSender;

    @Value("${resend.api.key}")
    private String apiKey;

    /**
     * Enviar email de bienvenida a un nuevo usuario
     */
    public boolean sendWelcomeEmail(String to, String userName) {
        String subject = "¡Bienvenido a LabMetricas!";
        String htmlContent = buildWelcomeEmailBody(userName);
        return sendEmail(to, subject, htmlContent);
    }

    /**
     * Enviar email de notificación de mantenimiento
     */
    public boolean sendMaintenanceNotification(String to, String userName, String maintenanceType, String scheduledDate) {
        String subject = "Notificación de Mantenimiento - LabMetricas";
        String htmlContent = buildMaintenanceNotificationBody(userName, maintenanceType, scheduledDate);
        return sendEmail(to, subject, htmlContent);
    }

    /**
     * Enviar email de recordatorio de contraseña
     */
    public boolean sendPasswordReminder(String to, String userName) {
        String subject = "Recordatorio de Contraseña - LabMetricas";
        String htmlContent = buildPasswordReminderBody(userName);
        return sendEmail(to, subject, htmlContent);
    }

    /**
     * Enviar email de confirmación de acción
     */
    public boolean sendActionConfirmation(String to, String userName, String action, String details) {
        String subject = "Confirmación de Acción - LabMetricas";
        String htmlContent = buildActionConfirmationBody(userName, action, details);
        return sendEmail(to, subject, htmlContent);
    }

    /**
     * Enviar email personalizado
     */
    public boolean sendCustomEmail(String to, String subject, String htmlContent) {
        return sendEmail(to, subject, htmlContent);
    }

    private boolean sendEmail(String to, String subject, String htmlContent) {
        try {
            logger.info("Sending production email to: {}", to);
            
            SendEmailRequest sendEmailRequest = SendEmailRequest.builder()
                .from(defaultSender)
                .to(to)
                .subject(subject)
                .html(htmlContent)
                .build();

            SendEmailResponse response = resend.emails().send(sendEmailRequest);
            
            logger.info("Production email sent successfully to: {} with ID: {}", to, response.getId());
            return true;
        } catch (Exception e) {
            logger.error("Failed to send production email to: {}", to, e);
            return false;
        }
    }

    private String buildWelcomeEmailBody(String userName) {
        return """
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: #f8f9fa; padding: 20px; border-radius: 0 0 5px 5px; }
                    .welcome { color: #28a745; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>LabMetricas</h1>
                    </div>
                    <div class="content">
                        <h2 class="welcome">¡Bienvenido, %s!</h2>
                        <p>Nos complace darte la bienvenida a LabMetricas, tu plataforma integral para la gestión de métricas y mantenimiento.</p>
                        <p>Con tu cuenta podrás:</p>
                        <ul>
                            <li>Gestionar equipos y mantenimientos</li>
                            <li>Monitorear métricas en tiempo real</li>
                            <li>Recibir notificaciones importantes</li>
                            <li>Acceder a reportes detallados</li>
                        </ul>
                        <p>Si tienes alguna pregunta, no dudes en contactarnos.</p>
                        <p><em>¡Gracias por elegir LabMetricas!</em></p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName);
    }

    private String buildMaintenanceNotificationBody(String userName, String maintenanceType, String scheduledDate) {
        return """
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #ffc107; color: #333; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: #f8f9fa; padding: 20px; border-radius: 0 0 5px 5px; }
                    .important { color: #dc3545; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Notificación de Mantenimiento</h1>
                    </div>
                    <div class="content">
                        <h2>Hola, %s</h2>
                        <p>Te informamos que se ha programado un mantenimiento en el sistema.</p>
                        <p><strong>Tipo de Mantenimiento:</strong> %s</p>
                        <p><strong>Fecha Programada:</strong> %s</p>
                        <p class="important">Por favor, asegúrate de guardar tu trabajo antes de la fecha indicada.</p>
                        <p>Te notificaremos cuando el mantenimiento esté completo.</p>
                        <p>Gracias por tu comprensión.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, maintenanceType, scheduledDate);
    }

    private String buildPasswordReminderBody(String userName) {
        return """
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #6c757d; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: #f8f9fa; padding: 20px; border-radius: 0 0 5px 5px; }
                    .reminder { color: #17a2b8; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Recordatorio de Contraseña</h1>
                    </div>
                    <div class="content">
                        <h2>Hola, %s</h2>
                        <p>Este es un recordatorio amigable para que actualices tu contraseña regularmente.</p>
                        <p class="reminder">Recomendaciones de seguridad:</p>
                        <ul>
                            <li>Usa contraseñas únicas y complejas</li>
                            <li>Cambia tu contraseña cada 3-6 meses</li>
                            <li>No compartas tu contraseña con nadie</li>
                            <li>Usa autenticación de dos factores si está disponible</li>
                        </ul>
                        <p>Si no solicitaste este recordatorio, puedes ignorarlo.</p>
                        <p>¡Mantén tu cuenta segura!</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName);
    }

    private String buildActionConfirmationBody(String userName, String action, String details) {
        return """
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: #f8f9fa; padding: 20px; border-radius: 0 0 5px 5px; }
                    .success { color: #28a745; font-weight: bold; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Confirmación de Acción</h1>
                    </div>
                    <div class="content">
                        <h2>Hola, %s</h2>
                        <p class="success">Tu acción ha sido procesada exitosamente.</p>
                        <p><strong>Acción realizada:</strong> %s</p>
                        <p><strong>Detalles:</strong> %s</p>
                        <p>Si no reconoces esta acción, por favor contacta al soporte técnico inmediatamente.</p>
                        <p>Gracias por usar LabMetricas.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(userName, action, details);
    }
}
