package com.labMetricas.LabMetricas.config;

import com.labMetricas.LabMetricas.role.model.Role;
import com.labMetricas.LabMetricas.role.model.RoleRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            // Crear roles si no existen
            createRoleIfNotFound("ADMIN");
            createRoleIfNotFound("SUPERVISOR");
            createRoleIfNotFound("OPERADOR");

            // Crear usuarios por defecto si no existen
            createDefaultUsers();

            logger.info("Database initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during database initialization: " + e.getMessage());
            throw e;
        }
    }

    private void createDefaultUsers() {
        // Administradores del Sistema
        createUserIfNotExists(
            "José", "García", 
            "jose.admin@labmetricas.com", 
            "Admin2024#Secure", 
            "ADMIN",
            "LabMetricas Central",
            "Ciudad de México",
            "5551234567",
            LocalDate.of(1988, 5, 15)
        );

        createUserIfNotExists(
            "María", "Rodríguez", 
            "maria.admin@labmetricas.com", 
            "AdminM2024$Safe", 
            "ADMIN",
            "LabMetricas Central",
            "Monterrey",
            "5552345678",
            LocalDate.of(1990, 8, 22)
        );

        // Supervisores
        createUserIfNotExists(
            "Carlos", "López", 
            "carlos.super@labmetricas.com", 
            "Super2024#Lab", 
            "SUPERVISOR",
            "LabMetricas Norte",
            "Guadalajara",
            "5553456789",
            LocalDate.of(1985, 3, 10)
        );

        createUserIfNotExists(
            "Ana", "Martínez", 
            "ana.super@labmetricas.com", 
            "Super2024$Control", 
            "SUPERVISOR",
            "LabMetricas Sur",
            "Puebla",
            "5554567890",
            LocalDate.of(1992, 7, 28)
        );

        // Operadores
        createUserIfNotExists(
            "Luis", "Hernández", 
            "luis.op@labmetricas.com", 
            "Oper2024#Lab", 
            "OPERADOR",
            "LabMetricas Norte",
            "Tijuana",
            "5555678901",
            LocalDate.of(1995, 6, 20)
        );

        createUserIfNotExists(
            "Laura", "Sánchez", 
            "laura.op@labmetricas.com", 
            "Oper2024$Work", 
            "OPERADOR",
            "LabMetricas Sur",
            "Mérida",
            "5556789012",
            LocalDate.of(1993, 9, 15)
        );

        // Usuarios adicionales para pruebas
        List<String[]> additionalUsers = Arrays.asList(
            new String[]{"Roberto", "Díaz", "roberto.super@labmetricas.com", "Super2024#Test", "SUPERVISOR", "LabMetricas Centro"},
            new String[]{"Patricia", "Flores", "patricia.op@labmetricas.com", "Oper2024#Test", "OPERADOR", "LabMetricas Norte"},
            new String[]{"Miguel", "Torres", "miguel.op@labmetricas.com", "Oper2024$Test", "OPERADOR", "LabMetricas Sur"}
        );

        for (String[] userData : additionalUsers) {
            createUserIfNotExists(
                userData[0], userData[1],
                userData[2], userData[3],
                userData[4], userData[5],
                "Ciudad de México",
                "555" + String.format("%07d", (int)(Math.random() * 10000000)),
                LocalDate.now().minusYears((long)(Math.random() * 30 + 20))
            );
        }
    }

    private void createUserIfNotExists(
            String name, 
            String lastname, 
            String email, 
            String password, 
            String roleName, 
            String companyName,
            String residence,
            String phone,
            LocalDate birthDate
    ) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setName(name);
            user.setLastname(lastname);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(roleRepository.findByName(roleName).orElseThrow());
            user.setEnabled(true);
            user.setStatus(true);
            user.setCompanyName(companyName);
            user.setResidence(residence);
            user.setPhone(phone);
            user.setBirthDate(birthDate);
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Created user: {} {} ({}) with role {}", name, lastname, email, roleName);
        }
    }

    private void createRoleIfNotFound(String name) {
        if (!roleRepository.existsByName(name)) {
            Role role = new Role(name);
            role.setCreatedAt(LocalDateTime.now());
            role.setUpdatedAt(LocalDateTime.now());
            roleRepository.save(role);
            logger.info("Created role: {}", name);
        }
    }
} 