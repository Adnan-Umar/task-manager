package com.adnanumar.task_manager.dto.response;

// Generic API wrapper for consistent response envelope across all endpoints
public record ApiResponse<T>(
        boolean success,
        String message,
        T data
) {
    // Convenience factory — success with data
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    // Convenience factory — success without data (e.g. delete operations)
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // Convenience factory — error response
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
