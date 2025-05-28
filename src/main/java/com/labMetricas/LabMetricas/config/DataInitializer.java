    package com.labMetricas.LabMetricas.config;

    import com.labMetricas.LabMetricas.role.model.Role;
    import com.labMetricas.LabMetricas.role.model.RoleRepository;
    import com.labMetricas.LabMetricas.user.model.User;
    import com.labMetricas.LabMetricas.user.model.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.boot.CommandLineRunner;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.stereotype.Component;

    import java.time.LocalDateTime;

    @Component
    public class DataInitializer implements CommandLineRunner {

        @Autowired
        private RoleRepository roleRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Override
        public void run(String... args) {
            // Crear roles si no existen
            createRoleIfNotFound("ADMIN");
            createRoleIfNotFound("SUPERVISOR");
            createRoleIfNotFound("OPERADOR");

            // Crear usuario admin por defecto
            if (!userRepository.existsByEmail("admin@labmetricas.com")) {
                User admin = new User();
                admin.setName("Administrador");
                admin.setLastname("Sistema");
                admin.setEmail("admin@labmetricas.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(roleRepository.findByName("ADMIN").orElseThrow());
                admin.setEnabled(true);
                admin.setStatus(true);
                admin.setCreatedAt(LocalDateTime.now());
                admin.setUpdatedAt(LocalDateTime.now());
                userRepository.save(admin);
            }
        }

        private void createRoleIfNotFound(String name) {
            if (!roleRepository.existsByName(name)) {
                Role role = new Role(name);
                role.setCreatedAt(LocalDateTime.now());
                role.setUpdatedAt(LocalDateTime.now());
                roleRepository.save(role);
            }
        }
    } 