package com.labMetricas.LabMetricas.config;

import com.labMetricas.LabMetricas.EquipmentCategory.model.EquipmentCategory;
import com.labMetricas.LabMetricas.EquipmentCategory.repository.EquipmentCategoryRepository;
import com.labMetricas.LabMetricas.MaintenanceProvider.model.MaintenanceProvider;
import com.labMetricas.LabMetricas.MaintenanceProvider.repository.MaintenanceProviderRepository;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.equipment.repository.EquipmentRepository;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.maintenance.model.ScheduledMaintenance;
import com.labMetricas.LabMetricas.maintenance.repository.MaintenanceRepository;
import com.labMetricas.LabMetricas.maintenance.repository.ScheduledMaintenanceRepository;
import com.labMetricas.LabMetricas.role.model.Role;
import com.labMetricas.LabMetricas.role.repository.RoleRepository;
import com.labMetricas.LabMetricas.user.model.User;
import com.labMetricas.LabMetricas.user.repository.UserRepository;
import com.labMetricas.LabMetricas.maintenance.model.dto.MaintenanceRequestDto;
import com.labMetricas.LabMetricas.MaintenanceType.model.MaintenanceType;
import com.labMetricas.LabMetricas.MaintenanceType.repository.MaintenanceTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentCategoryRepository equipmentCategoryRepository;

    @Autowired
    private MaintenanceProviderRepository maintenanceProviderRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private MaintenanceTypeRepository maintenanceTypeRepository;

    @Autowired
    private MaintenanceRepository maintenanceRepository;

    @Autowired
    private ScheduledMaintenanceRepository scheduledMaintenanceRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            // Initialize roles
            createRoleIfNotFound("ADMIN");
            createRoleIfNotFound("SUPERVISOR");
            createRoleIfNotFound("OPERADOR");

            // Create default users
            createDefaultUsers();

            createMaintenanceProviders();

            // Initialize equipment categories
            createEquipmentCategories();

            // Initialize equipment
            createEquipment();

            // Initialize maintenance types
            createMaintenanceTypes();

            // Initialize maintenances
            createMaintenances();

            logger.info("Complete database initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during database initialization: " + e.getMessage(), e);
            throw e;
        }
    }

    private void createEquipmentCategories() {
        List<String> categories = Arrays.asList(
            "Machinery", 
            "Electronic Equipment", 
            "Laboratory Instruments", 
            "Safety Equipment", 
            "Vehicles"
        );

        categories.forEach(categoryName -> {
            if (!equipmentCategoryRepository.findByNameIgnoreCase(categoryName).isPresent()) {
                EquipmentCategory category = new EquipmentCategory();
                category.setName(categoryName);
                category.setStatus(true);
                equipmentCategoryRepository.save(category);
                logger.info("Created equipment category: {}", categoryName);
            }
        });
    }

    private void createMaintenanceProviders() {
        List<String> maintenanceProviders = Arrays.asList(
                "Lab Métricas SAS de CV"
        );

        maintenanceProviders.forEach(maintenanceProviderName -> {
            if (!maintenanceProviderRepository.findByNameIgnoreCase(maintenanceProviderName).isPresent()) {
                MaintenanceProvider maintenanceProvider = new MaintenanceProvider();
                maintenanceProvider.setName(maintenanceProviderName);
                maintenanceProvider.setStatus(true);
                maintenanceProviderRepository.save(maintenanceProvider);
                logger.info("Created maintenance provider: {}", maintenanceProviderName);
            }
        });
    }

    private void createEquipment() {
        // Ensure we have users and categories first
        List<User> users = userRepository.findAll();
        List<EquipmentCategory> categories = equipmentCategoryRepository.findAll();
        List<MaintenanceProvider> maintenanceProviders = maintenanceProviderRepository.findAll();

        if (!users.isEmpty() && !categories.isEmpty()) {
            String[] equipmentNames = {
                "CNC Milling Machine", 
                "Spectrophotometer", 
                "Industrial Laser Cutter", 
                "Precision Balance", 
                "Safety Shower Station",
                "Electric Forklift",
                "Thermal Imaging Camera",
                "Robotic Arm",
                "Ultrasonic Cleaner",
                "Portable Generator"
            };

            for (int i = 0; i < equipmentNames.length; i++) {
                Equipment equipment = new Equipment();
                equipment.setName(equipmentNames[i]);
                equipment.setLocation("Main Facility - Zone " + (i % 3 + 1));
                equipment.setBrand("TechPro");
                equipment.setModel("Series " + (i + 1));
                equipment.setCode("" + (i + 1));
                equipment.setSerialNumber("Serie" + (i + 1));
                equipment.setRemarks("High-precision equipment for industrial use");
                equipment.setStatus(true);
                equipment.setCreatedAt(LocalDateTime.now());
                equipment.setUpdatedAt(LocalDateTime.now());
                
                // Cycle through users and categories
                equipment.setAssignedTo(users.get(i % users.size()));
                equipment.setEquipmentCategory(categories.get(i % categories.size()));
                equipment.setMaintenanceProvider(maintenanceProviders.get(i % maintenanceProviders.size()));

                equipmentRepository.save(equipment);
                logger.info("Created equipment: {}", equipmentNames[i]);
            }
        }
    }

    private void createMaintenanceTypes() {
        List<String> maintenanceTypes = Arrays.asList(
            "Preventive", 
            "Corrective"
        );

        maintenanceTypes.forEach(typeName -> {
            if (!maintenanceTypeRepository.findByName(typeName).isPresent()) {
                MaintenanceType type = new MaintenanceType(
                    typeName, 
                    "Standard " + typeName + " maintenance type"
                );
                maintenanceTypeRepository.save(type);
                logger.info("Created maintenance type: {}", typeName);
            }
        });
    }

    private void createMaintenances() {
        List<User> users = userRepository.findAll();
        List<Equipment> equipments = equipmentRepository.findAll();
        List<MaintenanceType> maintenanceTypes = maintenanceTypeRepository.findAll();

        if (!users.isEmpty() && !equipments.isEmpty() && !maintenanceTypes.isEmpty()) {
            // Reduce the number of maintenance requests to match available maintenance types
            MaintenanceRequestDto[] maintenanceRequests = {
                createMaintenanceRequestDto(
                    equipments.get(0), 
                    maintenanceTypes.get(0), 
                    users.get(0), 
                    "Routine calibration for precision equipment", 
                    MaintenanceRequestDto.Priority.LOW
                ),
                createMaintenanceRequestDto(
                    equipments.get(1), 
                    maintenanceTypes.get(1), 
                    users.get(1), 
                    "Urgent bearing replacement needed", 
                    MaintenanceRequestDto.Priority.HIGH
                )
            };

            // Simulate maintenance request creation
            for (MaintenanceRequestDto requestDto : maintenanceRequests) {
                try {
                    // Find the current user to simulate request creation
                    User currentUser = userRepository.findByEmail(requestDto.getResponsibleUserId().toString())
                        .orElse(users.get(0));

                    // Create maintenance request
                    Maintenance maintenance = new Maintenance();
                    maintenance.setDescription(requestDto.getDescription());
                    maintenance.setEquipment(equipmentRepository.findById(requestDto.getEquipmentId())
                        .orElseThrow(() -> new EntityNotFoundException("Equipment not found")));
                    maintenance.setMaintenanceType(maintenanceTypeRepository.findById(requestDto.getMaintenanceTypeId())
                        .orElseThrow(() -> new EntityNotFoundException("Maintenance Type not found")));
                    maintenance.setResponsible(currentUser);
                    maintenance.setCode(generateMaintenanceCode());
                    maintenance.setCreatedAt(LocalDateTime.now());
                    maintenance.setStatus(true);
                    maintenance.setPriority(Maintenance.Priority.valueOf(requestDto.getPriority().name()));

                    maintenanceRepository.save(maintenance);
                    logger.info("Created sample maintenance request: {}", maintenance.getCode());
                } catch (Exception e) {
                    logger.error("Error creating sample maintenance request", e);
                }
            }
        }
    }

    private MaintenanceRequestDto createMaintenanceRequestDto(
        Equipment equipment, 
        MaintenanceType maintenanceType, 
        User responsible, 
        String description, 
        MaintenanceRequestDto.Priority priority
    ) {
        MaintenanceRequestDto dto = new MaintenanceRequestDto();
        dto.setEquipmentId(equipment.getId());
        dto.setMaintenanceTypeId(maintenanceType.getId());
        dto.setResponsibleUserId(responsible.getId());
        dto.setDescription(description);
        dto.setPriority(priority);
        return dto;
    }

    private String generateMaintenanceCode() {
        return "MAINT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void createDefaultUsers() {
        // Administradores del Sistema
        createUserIfNotExists(
            "José García", 
            "jose.admin@labmetricas.com", 
            "Admin2024#Secure", 
            "ADMIN",
            "System Administrator"
        );

        createUserIfNotExists(
            "María Rodríguez", 
            "maria.admin@labmetricas.com", 
            "AdminM2024$Safe", 
            "ADMIN",
            "Chief Administrator"
        );

        // Supervisores
        createUserIfNotExists(
            "Carlos López", 
            "carlos.super@labmetricas.com", 
            "Super2024#Lab", 
            "SUPERVISOR",
            "Senior Supervisor"
        );

        createUserIfNotExists(
            "Ana Martínez", 
            "ana.super@labmetricas.com", 
            "Super2024$Control", 
            "SUPERVISOR",
            "Operations Supervisor"
        );

        // Operadores
        createUserIfNotExists(
            "Luis Hernández", 
            "luis.op@labmetricas.com", 
            "Oper2024#Lab", 
            "OPERADOR",
            "Field Operator"
        );

        createUserIfNotExists(
            "Laura Sánchez", 
            "laura.op@labmetricas.com", 
            "Oper2024$Work", 
            "OPERADOR",
            "Technical Operator"
        );

        // Usuarios adicionales para pruebas
        List<String[]> additionalUsers = Arrays.asList(
            new String[]{"Roberto Díaz", "roberto.super@labmetricas.com", "Super2024#Test", "SUPERVISOR", "Test Supervisor"},
            new String[]{"Patricia Flores", "patricia.op@labmetricas.com", "Oper2024#Test", "OPERADOR", "Test Operator"},
            new String[]{"Miguel Torres", "miguel.op@labmetricas.com", "Oper2024$Test", "OPERADOR", "Backup Operator"},
            new String[]{"Miguel Torres", "antonio734contacto@gmail.com", "Oper2024$Test", "OPERADOR", "Backup Operator"}        );

        for (String[] userData : additionalUsers) {
            createUserIfNotExists(
                userData[0],
                userData[1],
                userData[2],
                userData[3],
                userData[4]
            );
        }
    }

    private void createUserIfNotExists(
            String name, 
            String email, 
            String password, 
            String roleName,
            String position
    ) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setPosition(position);
            user.setRole(roleRepository.findByName(roleName).orElseThrow());
            user.setEnabled(true);
            user.setStatus(true);

            // Optional: Add phone number if needed
            // user.setPhone("5551234567");

            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            logger.info("Created user: {} ({}) with role {}", name, email, roleName);
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

    private void initializeMaintenanceTypes() {
        // Create Corrective Maintenance Type if not exists
        if (maintenanceTypeRepository.findByName("Corrective").isEmpty()) {
            MaintenanceType correctiveMaintenance = new MaintenanceType(
                "Corrective", 
                "Maintenance performed to correct or restore a failed or malfunctioning system"
            );
            maintenanceTypeRepository.save(correctiveMaintenance);
        }

        // Create Preventive Maintenance Type if not exists
        if (maintenanceTypeRepository.findByName("Preventive").isEmpty()) {
            MaintenanceType preventiveMaintenance = new MaintenanceType(
                "Preventive", 
                "Maintenance performed to prevent potential failures or degradation"
            );
            maintenanceTypeRepository.save(preventiveMaintenance);
        }
    }
} 