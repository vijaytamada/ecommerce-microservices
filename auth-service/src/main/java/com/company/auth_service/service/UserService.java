package com.company.auth_service.service;

import com.company.auth_service.dto.request.EmailChangeRequest;
import com.company.auth_service.dto.request.PasswordChangeRequest;
import com.company.auth_service.dto.response.ApiResponse;

public interface UserService {
    void changePassword(PasswordChangeRequest req);

    void changeEmail(EmailChangeRequest req);

    void verifyEmail();
}
