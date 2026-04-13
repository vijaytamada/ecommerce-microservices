package com.company.notification_service.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.UUID;

public final class HeaderUtils {
    private HeaderUtils() {}
    public static UUID getCurrentUserId() {
        String id = getHeader("X-User-Id");
        if (id == null || id.isBlank()) throw new IllegalStateException("X-User-Id header missing");
        return UUID.fromString(id);
    }
    private static String getHeader(String name) {
        ServletRequestAttributes a = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return a == null ? null : a.getRequest().getHeader(name);
    }
}
