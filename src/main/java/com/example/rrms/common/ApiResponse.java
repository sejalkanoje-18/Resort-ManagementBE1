package com.example.rrms.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Map;

public record ApiResponse<T>(boolean success, int status, String code, String
                             message,
                             T data, Map<String, String> fieldErrors, String path, Instant timestamp) {

    public static <T>ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return success(HttpStatus.OK, message, data);
    }
    public static ResponseEntity<ApiResponse<Void>> ok(String message) {
        return success(HttpStatus.OK, message, null);
    }
    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return success(HttpStatus.CREATED, message, data);
    }

    private static <T> ResponseEntity<ApiResponse<T>> success(HttpStatus s, String message, T data) {
        return ResponseEntity.status(s)
                .body(new ApiResponse<>(true, s.value(),null, message, data, null,
                        null, Instant.now()));
    }

    public static ApiResponse<Void> body(HttpStatus s, String code, String
                                         message,
                                         String path, Map<String, String> fieldErrors) {
        return new ApiResponse<>(false, s.value(), code, message, null,
                fieldErrors, path, Instant.now());
    }

    public static ResponseEntity<ApiResponse<Void>> error(HttpStatus s, String code,String message,
                                                          String path,
                                                          Map<String, String> fieldErrors) {
        return ResponseEntity.status(s).body(body(s, code, message, path, fieldErrors));
    }

}
