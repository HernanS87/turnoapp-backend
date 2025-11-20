package com.turnoapp.backend.config;

import com.turnoapp.backend.model.Professional;
import com.turnoapp.backend.model.User;
import com.turnoapp.backend.model.enums.UserRole;
import com.turnoapp.backend.repository.ProfessionalRepository;
import com.turnoapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {

    private final UserRepository userRepository;
    private final ProfessionalRepository professionalRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedData() {
        return args -> {
            // Only seed if database is empty
            if (userRepository.count() > 0) {
                log.info("Database already contains data, skipping seed");
                return;
            }

            log.info("Seeding initial data...");

            // Create Admin User
            User admin = User.builder()
                    .email("admin@turnoapp.com")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(UserRole.ADMIN)
                    .firstName("Admin")
                    .lastName("Sistema")
                    .phone("1234567890")
                    .build();
            userRepository.save(admin);
            log.info("Created admin user: {}", admin.getEmail());

            // Create Professional 1: María Rodríguez (Psicóloga)
            User mariaUser = User.builder()
                    .email("maria@psicologia.com")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .role(UserRole.PROFESSIONAL)
                    .firstName("María")
                    .lastName("Rodríguez")
                    .phone("1145678901")
                    .build();
            mariaUser = userRepository.save(mariaUser);

            Professional maria = Professional.builder()
                    .user(mariaUser)
                    .profession("Psicóloga")
                    .customUrl("maria-rodriguez")
                    .build();
            professionalRepository.save(maria);
            log.info("Created professional: {} - {}", maria.getFullName(), maria.getCustomUrl());

            // Create Professional 2: Juan Pérez (Dentista)
            User juanUser = User.builder()
                    .email("juan@dentista.com")
                    .passwordHash(passwordEncoder.encode("123456"))
                    .role(UserRole.PROFESSIONAL)
                    .firstName("Juan")
                    .lastName("Pérez")
                    .phone("1156789012")
                    .build();
            juanUser = userRepository.save(juanUser);

            Professional juan = Professional.builder()
                    .user(juanUser)
                    .profession("Odontólogo")
                    .customUrl("juan-perez")
                    .build();
            professionalRepository.save(juan);
            log.info("Created professional: {} - {}", juan.getFullName(), juan.getCustomUrl());

            log.info("Data seeding completed successfully!");
            log.info("===================================");
            log.info("CREDENTIALS FOR TESTING:");
            log.info("Admin: admin@turnoapp.com / admin123");
            log.info("Professional 1: maria@psicologia.com / 123456");
            log.info("Professional 2: juan@dentista.com / 123456");
            log.info("===================================");
        };
    }
}
