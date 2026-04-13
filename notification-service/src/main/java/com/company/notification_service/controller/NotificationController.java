package com.company.notification_service.controller;

import com.company.notification_service.document.Notification;
import com.company.notification_service.dto.response.ApiResponse;
import com.company.notification_service.repository.NotificationRepository;
import com.company.notification_service.utils.HeaderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification history APIs")
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/my")
    @Operation(summary = "Get my notification history")
    public ResponseEntity<ApiResponse<Page<Notification>>> getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UUID userId = HeaderUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Notifications fetched",
                notificationRepository.findByUserId(userId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }

    @GetMapping("/admin/all")
    @Operation(summary = "All notifications (ADMIN)")
    public ResponseEntity<ApiResponse<Page<Notification>>> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("All notifications fetched",
                notificationRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))));
    }
}
