package com.company.auth_service.jobs;

import com.company.auth_service.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository refreshRepo;

    /**
     * Runs once every day at 2 AM server time.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void removeExpiredTokens() {
        Instant now = Instant.now();
        int deletedCount = refreshRepo.deleteByExpiryDateBefore(now);
        System.out.println("Expired refresh tokens cleaned up: " + deletedCount);
    }
}