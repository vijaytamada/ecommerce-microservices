package com.company.auth_service.config;

import com.company.auth_service.entity.Role;
import com.company.auth_service.repository.RoleRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepo;

    public DataInitializer(RoleRepository roleRepo) {
        this.roleRepo = roleRepo;
    }


    @Override
    public void run(String... args) {

        if (roleRepo.findByName("ROLE_USER").isEmpty()) {
            roleRepo.save(Role.builder().name("ROLE_USER").build());
        }

        if (roleRepo.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepo.save(Role.builder().name("ROLE_ADMIN").build());
        }
    }
}
