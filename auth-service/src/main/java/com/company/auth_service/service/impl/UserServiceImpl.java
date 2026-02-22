package com.company.auth_service.service.impl;

import com.company.auth_service.dto.request.EmailChangeRequest;
import com.company.auth_service.dto.request.PasswordChangeRequest;
import com.company.auth_service.entity.User;
import com.company.auth_service.exception.AuthException;
import com.company.auth_service.exception.ResourceNotFoundException;
import com.company.auth_service.messaging.publisher.AuthEventPublisher;
import com.company.auth_service.repository.UserRepository;
import com.company.auth_service.service.UserService;
import com.company.auth_service.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthEventPublisher eventPublisher;


    @Override
    public void changePassword(PasswordChangeRequest req) {

        String email = SecurityUtils.getCurrentUserEmail();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));


        if (!passwordEncoder.matches(
                req.getOldPassword(),
                user.getPassword())) {

            throw new AuthException("Old password is incorrect");
        }

        user.setPassword(
                passwordEncoder.encode(req.getNewPassword())
        );

        userRepo.save(user);

        eventPublisher.publishPasswordChanged(user);
    }


    @Override
    public void changeEmail(EmailChangeRequest req) {

        String email = SecurityUtils.getCurrentUserEmail();

        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));


        if (userRepo.existsByEmail(req.getNewEmail())) {
            throw new AuthException("Email already in use");
        }

        user.setEmail(req.getNewEmail());
        user.setEmailVerified(false);

        userRepo.save(user);

        eventPublisher.publishEmailChanged(user);
    }


    @Override
    public void verifyEmail() {

        String email = SecurityUtils.getCurrentUserEmail();

        if (email == null) {
            throw new AuthException("No authenticated user found");
        }

        User user = userRepo.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));


        if (user.getEmailVerified()) {
            return; // already verified, no error
        }

        user.setEmailVerified(true);

        userRepo.save(user);

        eventPublisher.publishEmailVerified(user);
    }
}
