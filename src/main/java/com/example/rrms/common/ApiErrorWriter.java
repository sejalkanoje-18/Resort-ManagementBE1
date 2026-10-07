package com.example.rrms.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ApiErrorWriter {
        public static final String AUTH_ERROR_ATTR = "rrms.auth.error";

        private static final Map<String, String> MESSAGES = Map.of(
                "Token_EXPIRED" , "Access token has expired. Call POST /api/auth/refresh or log in again. ",
                "TOKEN_INVALID" ,  "Access token is invalid or malformed."  ,
                "TOKEN_REVOKED", "Token is no longer valid (logout, password change or role change). Log in again.",
                "TOKEN_WRONG_PURPOSE", "This token cannot call the API. An MFA token only works on /api/auth/mfa/setup and /api/auth/mfa/verify.",
                "TOKEN_TENANT_MISMATCH", "Token does not match the user's resort. Log in again.",
                "ACCOUNT_DISABLED", "Account is inactive or locked, or the resort is suspended.");

    private final ObjectMapper mapper;                           // Spring Boot's ObjectMapper (handles Instant)

    public void write(HttpServletRequest req, HttpServletResponse res,
                      HttpStatus status, String code, String message) throws IOException {
        res.setStatus(status.value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        mapper.writeValue(res.getOutputStream(), ApiResponse.body(status, code, message, req.getRequestURI(), null));
    }

    /** 401: uses the reason stored by the JWT filter, otherwise "no token". */
    public void unauthorized(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String code = (String) req.getAttribute(AUTH_ERROR_ATTR);
        if (code == null) {
            write(req, res, HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                    "Authentication required. Send the header: Authorization: Bearer <accessToken>");
        } else {
            write(req, res, HttpStatus.UNAUTHORIZED, code, MESSAGES.getOrDefault(code, "Authentication failed."));
        }
    }

    /** 403: valid token, but the URL role rule says no. */
    public void forbidden(HttpServletRequest req, HttpServletResponse res) throws IOException {
        write(req, res, HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission to access this resource.");
    }
}
