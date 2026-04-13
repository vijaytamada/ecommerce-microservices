package com.company.user_service.controller;

import com.company.user_service.dto.request.AddressRequest;
import com.company.user_service.dto.request.UpdateProfileRequest;
import com.company.user_service.dto.response.AddressResponse;
import com.company.user_service.dto.response.ApiResponse;
import com.company.user_service.dto.response.UserProfileResponse;
import com.company.user_service.service.UserProfileService;
import com.company.user_service.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile and address management APIs")
public class UserProfileController {

    private final UserProfileService userProfileService;

    /* -------------------- Profile -------------------- */

    @GetMapping("/me")
    @Operation(summary = "Get my profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile() {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", userProfileService.getProfile(userId)));
    }

    @PutMapping("/me")
    @Operation(summary = "Update my profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Profile updated", userProfileService.updateProfile(userId, request)));
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "Get profile by userId (Admin / internal use)")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfileById(@PathVariable UUID userId) {
        return ResponseEntity.ok(ApiResponse.success("Profile fetched", userProfileService.getProfile(userId)));
    }

    /* -------------------- Addresses -------------------- */

    @GetMapping("/me/addresses")
    @Operation(summary = "List my addresses")
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses() {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Addresses fetched", userProfileService.getAddresses(userId)));
    }

    @PostMapping("/me/addresses")
    @Operation(summary = "Add a new address")
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @Valid @RequestBody AddressRequest request) {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Address added", userProfileService.addAddress(userId, request)));
    }

    @PutMapping("/me/addresses/{addressId}")
    @Operation(summary = "Update an address")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Address updated",
                userProfileService.updateAddress(userId, addressId, request)));
    }

    @DeleteMapping("/me/addresses/{addressId}")
    @Operation(summary = "Delete an address")
    public ResponseEntity<ApiResponse<Void>> deleteAddress(@PathVariable Long addressId) {
        UUID userId = HeaderUtils.getCurrentUserId();
        userProfileService.deleteAddress(userId, addressId);
        return ResponseEntity.ok(ApiResponse.success("Address deleted", null));
    }

    @PutMapping("/me/addresses/{addressId}/default")
    @Operation(summary = "Set an address as default")
    public ResponseEntity<ApiResponse<AddressResponse>> setDefault(@PathVariable Long addressId) {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Default address updated",
                userProfileService.setDefaultAddress(userId, addressId)));
    }
}
