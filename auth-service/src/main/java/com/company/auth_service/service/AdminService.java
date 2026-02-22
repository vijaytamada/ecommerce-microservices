package com.company.auth_service.service;

import com.company.auth_service.dto.response.ApiResponse;
import com.company.auth_service.entity.Role;
import com.company.auth_service.entity.User;

import java.util.List;
import java.util.UUID;

public interface AdminService {
    List<User> viewAllUsers();

    void blockUser(UUID userId);

    void enableUser(UUID userId);

    void deleteUser(UUID userId);

    void assignRole(UUID userId, String roleName);

    void removeRole(UUID userId, String roleName);

    Role createRole(String roleName);
}