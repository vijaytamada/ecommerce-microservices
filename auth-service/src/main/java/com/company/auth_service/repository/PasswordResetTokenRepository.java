package com.company.auth_service.repository;

import com.company.auth_service.entity.PasswordResetToken;
import com.company.auth_service.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);

    void deleteByExpiryDateBefore(java.time.Instant now);
}

