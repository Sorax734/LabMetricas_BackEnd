package com.labMetricas.LabMetricas.config;

import com.labMetricas.LabMetricas.EquipmentCategory.model.EquipmentCategory;
import com.labMetricas.LabMetricas.EquipmentCategory.repository.EquipmentCategoryRepository;
import com.labMetricas.LabMetricas.MaintenanceProvider.model.MaintenanceProvider;
import com.labMetricas.LabMetricas.MaintenanceProvider.repository.MaintenanceProviderRepository;
import com.labMetricas.LabMetricas.equipment.model.Equipment;
import com.labMetricas.LabMetricas.equipment.repository.EquipmentRepository;
import com.labMetricas.LabMetricas.maintenance.model.Maintenance;
import com.labMetricas.LabMetricas.maintenance.model.ScheduledMaintenance;
import com.labMetricas.LabMetricas.maintenance.model.FrequencyType;
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

            // Initialize scheduled maintenances
            createScheduledMaintenances();

            logger.info("Complete database initialization completed successfully");
        } catch (Exception e) {
            logger.error("Error during database initialization: " + e.getMessage(), e);
            throw e;
        }
    }

    private void createEquipmentCategories() {
        List<String> categories = Arrays.asList(
            "Equipos de Medición", 
            "Equipos de Calibración", 
            "Equipos de Laboratorio", 
            "Equipos de Seguridad", 
            "Equipos Informáticos"
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
        List<MaintenanceProvider> providers = Arrays.asList(
            new MaintenanceProvider(
                null,
                true,
                "Lab Métricas SAS de CV",
                "Av. Tecnológico 123, Zona Industrial, CDMX",
                "55-1234-5678",
                "mantenimiento@labmetricas.com",
                "LME-2024-001",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null // Equipments
            ),
            new MaintenanceProvider(
                null,
                true,
                "Servicios Técnicos Especializados",
                "Calle Metrología 456, Monterrey, NL",
                "81-9876-5432",
                "contacto@serviciostecnicos.com",
                "STE-2024-002",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
            ),
            new MaintenanceProvider(
                null,
                true,
                "Calibración y Mantenimiento Industrial",
                "Blvd. Industrial 789, Guadalajara, Jal",
                "33-5555-7777",
                "info@calibracionindustrial.com",
                "CMI-2024-003",
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
            )
        );

        for (MaintenanceProvider provider : providers) {
            if (!maintenanceProviderRepository.findByNameIgnoreCase(provider.getName()).isPresent()) {
                maintenanceProviderRepository.save(provider);
                logger.info("Created maintenance provider: {}", provider.getName());
            }
        }
    }

    private void createEquipment() {
        // Ensure we have users and categories first
        List<User> users = userRepository.findAll();
        List<EquipmentCategory> categories = equipmentCategoryRepository.findAll();
        List<MaintenanceProvider> maintenanceProviders = maintenanceProviderRepository.findAll();

        if (!users.isEmpty() && !categories.isEmpty()) {
            // Solo 3 equipos, uno para cada usuario
            createEquipment("Micrómetro Digital", "Laboratorio Principal - Área A", "Mitutoyo", "MD-500", "MD001", "MIT-2024-001", "Micrómetro digital de alta precisión para mediciones de 0-25mm", categories.get(0), users.get(0), maintenanceProviders.get(0));
            createEquipment("Máquina de Medir por Coordenadas", "Sala de Calibración - Centro", "Zeiss", "MMC-500", "MMC001", "ZEI-2024-002", "Máquina de medición por coordenadas de alta precisión", categories.get(1), users.get(1), maintenanceProviders.get(1));
            createEquipment("Microscopio Metrológico", "Laboratorio Secundario - Mesa 1", "Olympus", "MM-600", "MM001", "OLY-2024-003", "Microscopio metrológico con cámara digital", categories.get(2), users.get(2), maintenanceProviders.get(2));
        }
    }

    private void createEquipment(String name, String location, String brand, String model, String code, String serialNumber, String remarks, EquipmentCategory category, User assignedTo, MaintenanceProvider maintenanceProvider) {
        // Check if equipment already exists by name
        if (equipmentRepository.findByName(name).isPresent()) {
            logger.info("Equipment already exists: {}", name);
            return;
        }

        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setLocation(location);
        equipment.setBrand(brand);
        equipment.setModel(model);
        equipment.setCode(code);
        equipment.setSerialNumber(serialNumber);
        equipment.setRemarks(remarks);
        equipment.setStatus(true);
        equipment.setCreatedAt(LocalDateTime.now());
        equipment.setUpdatedAt(LocalDateTime.now());
        equipment.setAssignedTo(assignedTo);
        equipment.setEquipmentCategory(category);
        equipment.setMaintenanceProvider(maintenanceProvider);

        equipmentRepository.save(equipment);
        logger.info("Created equipment: {}", name);
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
            // Create only one non-programmed maintenance (NP)
            MaintenanceRequestDto maintenanceRequest = createMaintenanceRequestDto(
                equipments.get(0), // Micrómetro Digital
                maintenanceTypes.get(1), // Corrective
                users.get(0), // Antonio García González
                "Reparación urgente del display digital del micrómetro - Error en lectura de mediciones", 
                MaintenanceRequestDto.Priority.HIGH
            );

            try {
                // Find the current user to simulate request creation
                User currentUser = userRepository.findByEmail(maintenanceRequest.getResponsibleUserId().toString())
                    .orElse(users.get(0));

                // Create maintenance request
                Maintenance maintenance = new Maintenance();
                maintenance.setDescription(maintenanceRequest.getDescription());
                maintenance.setEquipment(equipmentRepository.findById(maintenanceRequest.getEquipmentId())
                    .orElseThrow(() -> new EntityNotFoundException("Equipment not found")));
                maintenance.setMaintenanceType(maintenanceTypeRepository.findById(maintenanceRequest.getMaintenanceTypeId())
                    .orElseThrow(() -> new EntityNotFoundException("Maintenance Type not found")));
                maintenance.setResponsible(currentUser);
                maintenance.setCode(generateMaintenanceCode(maintenanceTypes.get(1), false)); // Non-programmed maintenance
                maintenance.setCreatedAt(LocalDateTime.now());
                maintenance.setStatus(true);
                maintenance.setPriority(Maintenance.Priority.valueOf(maintenanceRequest.getPriority().name()));

                maintenanceRepository.save(maintenance);
                logger.info("Created sample non-programmed maintenance request: {}", maintenance.getCode());
            } catch (Exception e) {
                logger.error("Error creating sample non-programmed maintenance request", e);
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
        // Administrador del Sistema
        createUserIfNotExists(
            "Antonio García González", 
            "antoniogarciagonzalez212@gmail.com", 
            "Admin2024#Secure", 
            "ADMIN",
            "Administrador del Sistema"
        );

        // Administrador del Sistema - Amador Casillas
        createUserIfNotExists(
            "Amador Casillas", 
            "amadorcasillasdr@gmail.com", 
            "Admin2024#Secure", 
            "ADMIN",
            "Administrador del Sistema"
        );

        // Supervisor
        createUserIfNotExists(
            "Supervisor UTEZ", 
            "20233tn106@utez.edu.mx", 
            "Super2024#Lab", 
            "SUPERVISOR",
            "Supervisor de Laboratorio"
        );

        // Operador
        createUserIfNotExists(
            "Desarrollador LabMétricas", 
            "labmetricasdev@gmail.com", 
            "Oper2024#Lab", 
            "OPERADOR",
            "Operador de Laboratorio"
        );
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

    private void createScheduledMaintenances() {
        List<User> users = userRepository.findAll();
        List<Equipment> equipments = equipmentRepository.findAll();
        List<MaintenanceType> maintenanceTypes = maintenanceTypeRepository.findAll();

        if (!users.isEmpty() && !equipments.isEmpty() && !maintenanceTypes.isEmpty()) {
            // Create only one programmed maintenance (P)
            createScheduledMaintenance(
                equipments.get(1), // Máquina de Medir por Coordenadas
                maintenanceTypes.get(0), // Preventive
                users.get(1), // Supervisor UTEZ
                "Calibración anual de máquina de medir por coordenadas - Verificación de trazabilidad metrológica",
                Maintenance.Priority.HIGH,
                "YEARLY",
                1,
                calculateNextMaintenanceDate("YEARLY", 1)
            );

            logger.info("Created 1 scheduled maintenance example");
        }
    }

    private void createScheduledMaintenance(
        Equipment equipment,
        MaintenanceType maintenanceType,
        User responsible,
        String description,
        Maintenance.Priority priority,
        String frequencyType,
        Integer frequencyValue,
        LocalDateTime nextMaintenanceDate
    ) {
        try {
            // Create maintenance request
            Maintenance maintenance = new Maintenance();
            maintenance.setDescription(description);
            maintenance.setEquipment(equipment);
            maintenance.setMaintenanceType(maintenanceType);
            maintenance.setResponsible(responsible);
            maintenance.setCode(generateMaintenanceCode(maintenanceType, true)); // Programmed maintenance
            maintenance.setCreatedAt(LocalDateTime.now());
            maintenance.setStatus(true);
            maintenance.setPriority(priority);

            // Save maintenance request
            Maintenance savedMaintenance = maintenanceRepository.save(maintenance);

            // Create scheduled maintenance
            ScheduledMaintenance scheduledMaintenance = new ScheduledMaintenance();
            scheduledMaintenance.setMaintenance(savedMaintenance);
            scheduledMaintenance.setNextMaintenance(nextMaintenanceDate);
            scheduledMaintenance.setFrequencyType(convertFrequencyType(frequencyType));
            scheduledMaintenance.setFrequencyValue(frequencyValue.shortValue());

            // Save scheduled maintenance
            scheduledMaintenanceRepository.save(scheduledMaintenance);

            logger.info("Created scheduled maintenance: {} for equipment: {}", 
                savedMaintenance.getCode(), equipment.getName());
        } catch (Exception e) {
            logger.error("Error creating scheduled maintenance for equipment: {}", equipment.getName(), e);
        }
    }

    private String generateScheduledMaintenanceCode() {
        return "SCHED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generateMaintenanceCode(MaintenanceType maintenanceType, boolean isProgrammed) {
        // Generate a unique maintenance code with the format: YYYY-MM-DD-TYPE-P/NP-COUNTER
        LocalDateTime now = LocalDateTime.now();
        String date = now.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        // Get maintenance type initials (first 3 characters)
        String typeInitials = getMaintenanceTypeInitials(maintenanceType);
        
        // Determine if it's programmed (P) or non-programmed (NP)
        String programType = isProgrammed ? "P" : "NP";
        
        // Get counter for the specific type
        String counter = getNextCounter(isProgrammed);
        
        return date + "-" + typeInitials + "-" + programType + "-" + counter;
    }
    
    private String getMaintenanceTypeInitials(MaintenanceType maintenanceType) {
        // Get the first 3 characters of the maintenance type name
        String name = maintenanceType.getName();
        if (name.length() >= 3) {
            return name.substring(0, 3).toUpperCase();
        } else {
            return name.toUpperCase();
        }
    }
    
    private String getNextCounter(boolean isProgrammed) {
        // Get the next counter for the specific program type
        long count;
        if (isProgrammed) {
            count = maintenanceRepository.countByScheduledMaintenanceIsNotNull();
        } else {
            count = maintenanceRepository.countByScheduledMaintenanceIsNull();
        }
        return String.format("%04d", count + 1);
    }

    private FrequencyType convertFrequencyType(String frequencyType) {
        return FrequencyType.valueOf(frequencyType);
    }

    private LocalDateTime calculateNextMaintenanceDate(String frequencyType, Integer frequencyValue) {
        LocalDateTime now = LocalDateTime.now();
        switch (frequencyType) {
            case "DAILY":
                return now.plusDays(frequencyValue);
            case "WEEKLY":
                return now.plusWeeks(frequencyValue);
            case "MONTHLY":
                return now.plusMonths(frequencyValue);
            case "YEARLY":
                return now.plusYears(frequencyValue);
            default:
                return now.plusMonths(1);
        }
    }
} 