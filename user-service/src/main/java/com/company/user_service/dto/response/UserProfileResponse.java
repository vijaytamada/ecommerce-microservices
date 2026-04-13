package com.company.user_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatarUrl;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
