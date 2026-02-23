package com.company.auth_service.service.impl;

import com.company.auth_service.dto.response.UserResponse;
import com.company.auth_service.entity.Role;
import com.company.auth_service.entity.User;
import com.company.auth_service.exception.ResourceAlreadyExistsException;
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
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;


    @Override
    public List<UserResponse> viewAllUsers() {

        return userRepo.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
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
            throw new ResourceAlreadyExistsException("Already role assigned!");
        }

        Role role = Role.builder()
                .name(roleName)
                .build();

        return roleRepo.save(role);
    }

    // Mappers
    private UserResponse mapToDTO(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .emailVerified(user.getEmailVerified())
                .roles(
                        user.getRoles()
                                .stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet())
                )
                .build();
    }
}
