package com.company.auth_service.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private String status;     // SUCCESS / ERROR
    private String message;    // Human readable
    private T data;            // Payload
    private String error;      // Error code / message
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String error) {
        return ApiResponse.<T>builder()
                .status("ERROR")
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
