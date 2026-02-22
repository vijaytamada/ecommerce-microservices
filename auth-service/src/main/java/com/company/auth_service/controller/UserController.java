package com.company.auth_service.controller;

import com.company.auth_service.dto.request.EmailChangeRequest;
import com.company.auth_service.dto.request.PasswordChangeRequest;
import com.company.auth_service.dto.response.ApiResponse;
import com.company.auth_service.service.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    /* Change Password */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody PasswordChangeRequest request) {

        userService.changePassword(request);

        return ResponseEntity.ok(
                ApiResponse.success("Password changed successfully", null)
        );
    }


    /* Change Email */
    @PostMapping("/change-email")
    public ResponseEntity<ApiResponse<Void>> changeEmail(
            @RequestBody EmailChangeRequest request) {

        userService.changeEmail(request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Email changed. Verification required.",
                        null
                )
        );
    }


    /* Verify Email */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail() {

        userService.verifyEmail();

        return ResponseEntity.ok(
                ApiResponse.success("Email verified successfully", null)
        );
    }
}
