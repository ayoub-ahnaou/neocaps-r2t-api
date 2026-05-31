package com.neocaps.api.config;

import com.neocaps.api.enums.UserRole;
import com.neocaps.api.model.dto.UserCreateRequest;
import com.neocaps.api.repository.UserRepository;
import com.neocaps.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Database is empty. Initializing default users...");

            // Seed Admin
            UserCreateRequest adminRequest = new UserCreateRequest();
            adminRequest.setUsername("admin");
            adminRequest.setPassword("admin123");
            adminRequest.setRole(UserRole.ADMIN);
            userService.createUser(adminRequest);
            log.info("Default administrator created (admin/admin123)");

            // Seed Operator
            UserCreateRequest operatorRequest = new UserCreateRequest();
            operatorRequest.setUsername("operator");
            operatorRequest.setPassword("operator123");
            operatorRequest.setRole(UserRole.OPERATOR);
            userService.createUser(operatorRequest);
            log.info("Default operator created (operator/operator123)");

            // Seed Supervisor
            UserCreateRequest supervisorRequest = new UserCreateRequest();
            supervisorRequest.setUsername("supervisor");
            supervisorRequest.setPassword("supervisor123");
            supervisorRequest.setRole(UserRole.SUPERVISOR);
            userService.createUser(supervisorRequest);
            log.info("Default supervisor created (supervisor/supervisor123)");
        } else {
            log.info("Database already contains users. Skipping seeder.");
        }
    }
}
