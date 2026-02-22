package com.company.auth_service.service.impl;

import com.company.auth_service.entity.Role;
import com.company.auth_service.entity.User;
import com.company.auth_service.exception.ResourceNotFoundException;
import com.company.auth_service.exception.AuthException;
import com.company.auth_service.repository.RoleRepository;
import com.company.auth_service.repository.UserRepository;
import com.company.auth_service.service.AdminService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;


    @Override
    public List<User> viewAllUsers() {

        return userRepo.findAll();
    }


    @Override
    public void blockUser(UUID userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setEnabled(false);
        userRepo.save(user);
    }


    @Override
    public void enableUser(UUID userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setEnabled(true);
        userRepo.save(user);
    }


    @Override
    public void deleteUser(UUID userId) {

        if (!userRepo.existsById(userId)) {
            throw new ResourceNotFoundException("User not found");
        }

        userRepo.deleteById(userId);
    }


    @Override
    public void assignRole(UUID userId, String roleName) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found"));

        user.getRoles().add(role);
        userRepo.save(user);
    }


    @Override
    public void removeRole(UUID userId, String roleName) {

        User user = userRepo.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        Role role = roleRepo.findByName(roleName)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found"));

        user.getRoles().remove(role);
        userRepo.save(user);
    }


    @Override
    public Role createRole(String roleName) {

        if (roleRepo.existsByName(roleName)) {
            throw new AuthException("Role already exists");
        }

        Role role = Role.builder()
                .name(roleName)
                .build();

        return roleRepo.save(role);
    }
}
