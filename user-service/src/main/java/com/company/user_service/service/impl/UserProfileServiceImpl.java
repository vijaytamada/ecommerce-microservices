package com.company.user_service.service.impl;

import com.company.user_service.dto.request.AddressRequest;
import com.company.user_service.dto.request.UpdateProfileRequest;
import com.company.user_service.dto.response.AddressResponse;
import com.company.user_service.dto.response.UserProfileResponse;
import com.company.user_service.entity.Address;
import com.company.user_service.entity.UserProfile;
import com.company.user_service.exception.ResourceNotFoundException;
import com.company.user_service.repository.AddressRepository;
import com.company.user_service.repository.UserProfileRepository;
import com.company.user_service.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileRepository profileRepository;
    private final AddressRepository addressRepository;

    @Override
    public UserProfileResponse getProfile(UUID userId) {
        UserProfile profile = findProfileOrThrow(userId);
        return toProfileResponse(profile);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(UUID userId, UpdateProfileRequest request) {
        UserProfile profile = findProfileOrThrow(userId);

        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName()  != null) profile.setLastName(request.getLastName());
        if (request.getPhone()     != null) profile.setPhone(request.getPhone());
        if (request.getAvatarUrl() != null) profile.setAvatarUrl(request.getAvatarUrl());

        return toProfileResponse(profileRepository.save(profile));
    }

    @Override
    public List<AddressResponse> getAddresses(UUID userId) {
        return addressRepository.findByUserId(userId)
                .stream()
                .map(this::toAddressResponse)
                .toList();
    }

    @Override
    @Transactional
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        // If this is the first address or marked as default, clear existing defaults
        if (request.isDefault()) {
            addressRepository.clearDefaultForUser(userId);
        }

        Address address = Address.builder()
                .userId(userId)
                .label(request.getLabel() != null ? request.getLabel() : Address.AddressLabel.HOME)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .street(request.getStreet())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .zipCode(request.getZipCode())
                .isDefault(request.isDefault())
                .build();

        return toAddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(UUID userId, Long addressId, AddressRequest request) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));

        if (request.getLabel()    != null) address.setLabel(request.getLabel());
        if (request.getFullName() != null) address.setFullName(request.getFullName());
        if (request.getPhone()    != null) address.setPhone(request.getPhone());
        if (request.getStreet()   != null) address.setStreet(request.getStreet());
        if (request.getCity()     != null) address.setCity(request.getCity());
        if (request.getState()    != null) address.setState(request.getState());
        if (request.getCountry()  != null) address.setCountry(request.getCountry());
        if (request.getZipCode()  != null) address.setZipCode(request.getZipCode());

        if (request.isDefault()) {
            addressRepository.clearDefaultForUser(userId);
            address.setDefault(true);
        }

        return toAddressResponse(addressRepository.save(address));
    }

    @Override
    @Transactional
    public void deleteAddress(UUID userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));
        addressRepository.delete(address);
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(UUID userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found: " + addressId));

        addressRepository.clearDefaultForUser(userId);
        address.setDefault(true);
        return toAddressResponse(addressRepository.save(address));
    }

    /* ---------- Helpers ---------- */

    private UserProfile findProfileOrThrow(UUID userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found for userId: " + userId));
    }

    private UserProfileResponse toProfileResponse(UserProfile p) {
        return UserProfileResponse.builder()
                .userId(p.getUserId())
                .email(p.getEmail())
                .firstName(p.getFirstName())
                .lastName(p.getLastName())
                .phone(p.getPhone())
                .avatarUrl(p.getAvatarUrl())
                .active(p.isActive())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private AddressResponse toAddressResponse(Address a) {
        return AddressResponse.builder()
                .id(a.getId())
                .label(a.getLabel())
                .fullName(a.getFullName())
                .phone(a.getPhone())
                .street(a.getStreet())
                .city(a.getCity())
                .state(a.getState())
                .country(a.getCountry())
                .zipCode(a.getZipCode())
                .isDefault(a.isDefault())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
