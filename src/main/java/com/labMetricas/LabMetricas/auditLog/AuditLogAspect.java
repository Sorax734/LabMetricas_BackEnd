package com.labMetricas.LabMetricas.auditLog;

import com.labMetricas.LabMetricas.auditLog.model.AuditLog;
import com.labMetricas.LabMetricas.auditLog.repository.AuditLogRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private UserRepository userRepository;

    // Intercepta todos los métodos de cualquier clase en el paquete controller
    @Pointcut("within(com.labMetricas.LabMetricas..controller..*)")
    public void controllerMethods() {}

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        String username = getCurrentUsername();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        // Buscar el usuario por email (username)
        Optional<User> userOpt = userRepository.findByEmail(username);
        User user = userOpt.orElse(null);

        // Buscar si algún parámetro es un Document
        com.labMetricas.LabMetricas.document.model.Document document = null;
        for (Object arg : args) {
            if (arg instanceof com.labMetricas.LabMetricas.document.model.Document) {
                document = (com.labMetricas.LabMetricas.document.model.Document) arg;
                break;
            }
        }

        // Mensaje personalizado
        String message = buildCustomMessage(user != null ? user.getName() : username, methodName, className, args, result);

        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(message);
        log.setCreatedAt(java.time.LocalDateTime.now());
        if (document != null) {
            log.setDocument(document);
        }
        auditLogRepository.save(log);
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        return "ANONYMOUS";
    }

    private String buildCustomMessage(String username, String methodName, String className, Object[] args, Object result) {
        // Ejemplo: "Manuel agregó un equipo: EquipoDto{nombre='Microscopio'}"
        String entity = extractEntityName(className);
        String action = mapMethodToAction(methodName);
        String params = Arrays.toString(args);
        return String.format("%s %s en %s con parámetros %s", username, action, entity, params);
    }

    private String extractEntityName(String className) {
        // Quita el sufijo Controller si existe
        if (className.endsWith("Controller")) {
            return className.substring(0, className.length() - "Controller".length());
        }
        return className;
    }

    private String mapMethodToAction(String methodName) {
        // Puedes personalizar más acciones aquí
        if (methodName.startsWith("create") || methodName.startsWith("add")) {
            return "agregó";
        } else if (methodName.startsWith("update") || methodName.startsWith("edit")) {
            return "actualizó";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "eliminó";
        } else if (methodName.startsWith("get") || methodName.startsWith("find")) {
            return "consultó";
        }
        return "ejecutó la acción '" + methodName + "'";
    }
} 