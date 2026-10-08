package com.example.rrms.common;


import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Response;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.security.sasl.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.rrms.common.ApiResponse.error;
import static jdk.internal.org.jline.utils.Colors.s;
import static org.springframework.http.RequestEntity.method;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>>
    validation(MethodArgumentNotValidException ex,
               HttpServletRequest req) {
        Map<String , String> fields = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe ->
        fields.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Validation failed.see 'fieldErrors'.",
                req.getRequestURI(), fields);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>>
    unreadable(HttpServletRequest req) {
        return error(HttpStatus.BAD_REQUEST,
                "MALFORMED_REQUEST_BODY",
                "Request body is missing or is not valid JSON." +
                        "Check quotes/commas and enum values(e.g. staffType).",reg);

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
                public ResponseEntity<ApiResponse<Void>>
                badParam(MethodArgumentTypeMismatchException ex,
                HttpServlet req) {
            return error(HttpStatus.BAD_REQUEST,
                    "INVALID_PARAMETER",
                    "Parameter '" + ex.getName() + "' has an invalid value.", req)
        }
        @ExceptionHandler (HttpMediaTypeNotSupportException.class)
                public ResponseEntity<ApiResponse<Void>>
                method(HttpSerrvletRequest req) {
            return error(HttpStatus.METHOD_NOT_ALLOWED,
                    "METHOD_NOT_ALLOWER",
                    "HTTp method"+ req.getMethod()+"is not support for this URL.",req
                            );

        }

        @ExceptionHandler(NoResponceFoundExeption.class)
                public ResponseEntity<ApiResponse<Void>>
                noRoute(HttpServletRequest erq) {
            return error(HttpStatus.NOT_FOUND,"ROUTE_NOT_FOUND",
                    "No endpoint for" + req.getMethod()+""+
                    req.getRequestURI()+".Check the URL.",req);    {
                return error(HttpStatus.NOT_FOUND, "ROUTE_NOT_FOUND",
                        "No endpoint for " + req.getMethod() + " " + req.getRequestURI() + ". Check the URL.", req);
            }


            @ExceptionHandler(BadCredentialsException.class)
            public ResponseEntity<ApiResponse<Void>> badCredentials(BadCredentialsException ex, HttpServletRequest req) {
                return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), req);   // messages are ours + generic
            }

            @ExceptionHandler(LockedException.class)
            public ResponseEntity<ApiResponse<Void>> locked(LockedException ex, HttpServletRequest req) {
                return error(HttpStatus.LOCKED, "ACCOUNT_LOCKED", ex.getMessage(), req);              // 423
            }

            @ExceptionHandler(AuthenticationException.class)
            public ResponseEntity<ApiResponse<Void>> authentication(HttpServletRequest req) {
                return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication failed.", req);
            }

            /** @PreAuthorize denials and the AccessDeniedException thrown by our services/CurrentUser. */
            @ExceptionHandler(AccessDeniedException.class)
            public ResponseEntity<ApiResponse<Void>> denied(AccessDeniedException ex, HttpServletRequest req) {
                return error(HttpStatus.FORBIDDEN, "FORBIDDEN", ex.getMessage(), req);
            }


            @ExceptionHandler(ApiException.class)
            public ResponseEntity<ApiResponse<Void>> business(ApiException ex, HttpServletRequest req) {
                return error(ex.getStatus(), ex.getCode(), ex.getMessage(), req);
            }

            @ExceptionHandler(EntityNotFoundException.class)
            public ResponseEntity<ApiResponse<Void>> notFound(EntityNotFoundException ex, HttpServletRequest req) {
                return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), req);
            }

            @ExceptionHandler(DataIntegrityViolationException.class)
            public ResponseEntity<ApiResponse<Void>> dataConflict(DataIntegrityViolationException ex, HttpServletRequest req) {
                log.warn("Data integrity violation on {} {}: {}", req.getMethod(), req.getRequestURI(), ex.getMostSpecificCause().getMessage());
                return error(HttpStatus.CONFLICT, "DATA_CONFLICT",
                        "A record with the same unique value already exists, or required data is missing.", req);
            }


            @ExceptionHandler(Exception.class)
            public ResponseEntity<ApiResponse<Void>> unexpected(Exception ex, HttpServletRequest req) {
                String ref = UUID.randomUUID().toString().substring(0, 8);
                log.error("Unhandled error [{}] on {} {}", ref, req.getMethod(), req.getRequestURI(), ex);
                return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                        "Something went wrong. Reference: " + ref + " (search the server log for it).", req);
            }

            private static ResponseEntity<ApiResponse<Void>> error(HttpStatus s, String code, String msg, HttpServletRequest req) {
                return ApiResponse.error(s, code, msg, req.getRequestURI(), null);
            }
        }
