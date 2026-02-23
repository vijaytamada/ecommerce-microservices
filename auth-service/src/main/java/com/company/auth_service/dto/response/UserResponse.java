package com.company.auth_service.dto.response;

import lombok.*;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UserResponse {
    private UUID id;
    private String email;
    private Boolean enabled;
    private Boolean emailVerified;
    private Set<String> roles;
}
