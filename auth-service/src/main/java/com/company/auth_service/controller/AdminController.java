package com.company.auth_service.controller;

import com.company.auth_service.dto.response.ApiResponse;
import com.company.auth_service.dto.response.UserResponse;
import com.company.auth_service.entity.Role;
import com.company.auth_service.entity.User;
import com.company.auth_service.service.AdminService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    /* Get All Users */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> viewAllUsers() {

        List<UserResponse> users = adminService.viewAllUsers();

        return ResponseEntity.ok(
                ApiResponse.success("Fetched all users", users)
        );
    }


    /* Block User */
    @PostMapping("/users/{id}/block")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @PathVariable UUID id) {

        adminService.blockUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User blocked successfully", null)
        );
    }


    /* Enable User */
    @PostMapping("/users/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enableUser(
            @PathVariable UUID id) {

        adminService.enableUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User enabled successfully", null)
        );
    }


    /* Delete User */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable UUID id) {

        adminService.deleteUser(id);

        return ResponseEntity.ok(
                ApiResponse.success("User deleted successfully", null)
        );
    }


    /* Assign Role */
    @PostMapping("/users/{id}/roles/{roleName}")
    public ResponseEntity<ApiResponse<Void>> assignRole(
            @PathVariable UUID id,
            @PathVariable String roleName) {

        adminService.assignRole(id, roleName);

        return ResponseEntity.ok(
                ApiResponse.success("Role assigned successfully", null)
        );
    }


    /* Remove Role */
    @DeleteMapping("/users/{id}/roles/{roleName}")
    public ResponseEntity<ApiResponse<Void>> removeRole(
            @PathVariable UUID id,
            @PathVariable String roleName) {

        adminService.removeRole(id, roleName);

        return ResponseEntity.ok(
                ApiResponse.success("Role removed successfully", null)
        );
    }


    /* Create Role */
    @PostMapping("/roles/{roleName}")
    public ResponseEntity<ApiResponse<Role>> createRole(
            @PathVariable String roleName) {

        Role role = adminService.createRole(roleName);

        return ResponseEntity.ok(
                ApiResponse.success("Role created successfully", role)
        );
    }
}
