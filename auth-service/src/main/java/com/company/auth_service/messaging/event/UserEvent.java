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
public class UserEvent implements Serializable {

    private String eventType;

    private UUID userId;

    private String email;

    private Instant timestamp;
}
