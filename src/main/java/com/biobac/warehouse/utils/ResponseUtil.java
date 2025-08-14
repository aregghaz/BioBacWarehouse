package com.biobac.warehouse.utils;

import com.biobac.warehouse.response.ApiResponse;

public class ResponseUtil {

    public static <T> ApiResponse<T> success(String message, T data, Object metadata) {
        return new ApiResponse<>(true, message, data, metadata);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return success(message, data, null);
    }

    public static <T> ApiResponse<T> success(String message) {
        return success(message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }
}
