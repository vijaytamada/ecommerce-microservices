package com.company.auth_service.messaging.event;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetEvent implements Serializable {

    private UUID userId;

    private String email;

    private String token;

    private Instant expiresAt;
}
