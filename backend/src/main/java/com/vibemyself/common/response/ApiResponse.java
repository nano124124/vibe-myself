package com.vibemyself.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).build();
    }

    public static ApiResponse<Void> ok() {
        return ApiResponse.<Void>builder().success(true).build();
    }

    public static ApiResponse<Void> fail(String message) {
        return ApiResponse.<Void>builder().success(false).message(message).build();
    }
}
