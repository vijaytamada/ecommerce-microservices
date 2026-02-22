package com.company.auth_service.repository;

import com.company.auth_service.entity.RefreshToken;
import com.company.auth_service.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndRevokedFalse(User user);

    void deleteByUser(User user);

    @Transactional
    int deleteByExpiryDateBefore(Instant now);
}
