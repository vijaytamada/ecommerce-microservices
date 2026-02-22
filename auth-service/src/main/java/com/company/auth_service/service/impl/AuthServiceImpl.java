package com.company.auth_service.service.impl;

import com.company.auth_service.dto.request.*;
import com.company.auth_service.dto.response.*;

import com.company.auth_service.entity.PasswordResetToken;
import com.company.auth_service.entity.RefreshToken;
import com.company.auth_service.entity.Role;
import com.company.auth_service.entity.User;
import com.company.auth_service.repository.*;
import com.company.auth_service.security.JwtUtil;
import com.company.auth_service.service.AuthService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import com.company.auth_service.messaging.publisher.AuthEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final RefreshTokenRepository refreshRepo;
    private final PasswordResetTokenRepository resetRepo;

    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;

    private final AuthEventPublisher eventPublisher;

    @Value("${jwt.refresh-expiry-days}")
    private Long refreshExpiryDays;

    /* ---------------- REGISTER ---------------- */

    @Override
    public AuthResponse register(RegisterRequest request) {

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        Role userRole = roleRepo.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("ROLE_USER not found"));

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .enabled(true)
                .emailVerified(false)
                .roles(Set.of(userRole))
                .build();

        userRepo.save(user);
        eventPublisher.publishUserCreated(user);

        return generateAuthResponse(user);
    }

    /* ---------------- LOGIN ---------------- */

    @Override
    public AuthResponse login(LoginRequest request) {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                );

        authManager.authenticate(authentication);

        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow();

        user.setLastLogin(Instant.now());

        // Remove all previous refresh tokens for this user & logs out of all devices
        // refreshRepo.deleteByUser(user);

        return generateAuthResponse(user);
    }

    /* ---------------- REFRESH ---------------- */

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {

        RefreshToken refreshToken = refreshRepo
                .findByToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (refreshToken.getRevoked()
                || refreshToken.getExpiryDate().isBefore(Instant.now())) {

            throw new RuntimeException("Refresh token expired");
        }

        User user = refreshToken.getUser();

        // Generate ONLY new access token
        String newAccessToken = jwtUtil.generateAccessToken(user);

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken.getToken()) // SAME refresh
                .email(user.getEmail())
                .roles(roles)
                .build();
    }

    /* ---------------- LOGOUT ---------------- */

    @Override
    public void logout(String refreshToken) {

        refreshRepo.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshRepo.save(token);
                });
    }

    /* ---------------- FORGOT PASSWORD ---------------- */

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        resetRepo.deleteByUser(user);

        // Generate token
        String token = UUID.randomUUID().toString();

        Instant expiry = Instant.now().plus(15, ChronoUnit.MINUTES);

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .token(token)
                        .user(user)
                        .expiryDate(expiry)
                        .used(false)
                        .build();

        resetRepo.save(resetToken);

        // Send async email
        eventPublisher.publishPasswordResetRequested(
                user,
                token,
                expiry
        );
    }

    /* ---------------- RESET PASSWORD ---------------- */

    @Override
    public void resetPassword(ResetPasswordRequest request) {

        PasswordResetToken resetToken = resetRepo
                .findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (resetToken.getUsed()
                || resetToken.getExpiryDate().isBefore(Instant.now())) {

            throw new RuntimeException("Token expired");
        }

        User user = resetToken.getUser();

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        resetToken.setUsed(true);

        userRepo.save(user);
        resetRepo.save(resetToken);
        eventPublisher.publishPasswordChanged(user);
    }

    /* ---------------- TOKEN GENERATION ---------------- */

    private AuthResponse generateAuthResponse(User user) {

        String accessToken = jwtUtil.generateAccessToken(user);

        String refreshTokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .expiryDate(
                        Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS)
                )
                .revoked(false)
                .build();

        refreshRepo.save(refreshToken);

        List<String> roles = user.getRoles()
                .stream()
                .map(Role::getName)
                .toList();

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .email(user.getEmail())
                .roles(roles)
                .build();
    }
}

