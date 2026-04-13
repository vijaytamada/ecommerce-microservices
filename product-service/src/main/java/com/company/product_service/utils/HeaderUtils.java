package com.company.product_service.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

public final class HeaderUtils {

    private HeaderUtils() {}

    public static UUID getCurrentUserId() {
        String userId = getHeader("X-User-Id");
        if (userId == null || userId.isBlank()) {
            throw new IllegalStateException("X-User-Id header is missing");
        }
        return UUID.fromString(userId);
    }

    public static String getCurrentUserEmail() {
        return getHeader("X-User-Email");
    }

    public static boolean isAdmin() {
        String roles = getHeader("X-User-Roles");
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    private static String getHeader(String name) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        HttpServletRequest request = attrs.getRequest();
        return request.getHeader(name);
    }
}
