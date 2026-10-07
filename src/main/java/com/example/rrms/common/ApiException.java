package com.example.rrms.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    public ApiException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static ApiException badRequest(String code, String msg) {
        return new ApiException(HttpStatus.BAD_REQUEST, code, msg); }
    public static ApiException conflict(String code, String msg) {
        return new ApiException(HttpStatus.CONFLICT, code, msg); }
    public static ApiException notFound(String code, String msg) {
        return new ApiException(HttpStatus.NOT_FOUND, code, msg);
    }
}
