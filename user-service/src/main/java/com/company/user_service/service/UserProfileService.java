package com.company.user_service.service;

import com.company.user_service.dto.request.AddressRequest;
import com.company.user_service.dto.request.UpdateProfileRequest;
import com.company.user_service.dto.response.AddressResponse;
import com.company.user_service.dto.response.UserProfileResponse;

import java.util.List;
import java.util.UUID;

public interface UserProfileService {

    UserProfileResponse getProfile(UUID userId);

    UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request);

    List<AddressResponse> getAddresses(UUID userId);

    AddressResponse addAddress(UUID userId, AddressRequest request);

    AddressResponse updateAddress(UUID userId, Long addressId, AddressRequest request);

    void deleteAddress(UUID userId, Long addressId);

    AddressResponse setDefaultAddress(UUID userId, Long addressId);
}
