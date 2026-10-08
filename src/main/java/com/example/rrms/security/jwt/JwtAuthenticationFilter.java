package com.example.rrms.security.jwt;

import com.example.rrms.security.user.CustomUserDetailsService;
import com.example.rrms.security.user.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/login") ||
               path.startsWith("/api/auth/mfa/verify") ||
               path.startsWith("/api/auth/refresh") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-ui");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = header.substring(7).trim();
        while (token.toLowerCase().startsWith("bearer ")) {
            token = token.substring(7).trim();
        }

        try {
            Claims c = jwtService.parse(token);

            // 1. Only Access tokens authenticate API calls
            if (!JwtService.PURPOSE_ACCESS.equals(c.get("purpose", String.class))) {
                request.setAttribute(com.example.rrms.common.ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_WRONG_PURPOSE");
                throw new JwtException("Wrong token purpose");
            }

            // 2. Load the current user state from DB
            UserPrincipal p = userDetailsService.loadUserById(Long.valueOf(c.getSubject()));

            // 3. Cross-check token vs DB
            if (!p.isEnabled() || !p.isAccountNonLocked()) {
                request.setAttribute(com.example.rrms.common.ApiErrorWriter.AUTH_ERROR_ATTR, "ACCOUNT_DISABLED");
                throw new JwtException("Account disabled, tenant suspended, or account locked");
            }

            Long tokenTenant = JwtService.longClaim(c, "tenantId");
            if (!Objects.equals(tokenTenant, p.getTenantId())) {
                request.setAttribute(com.example.rrms.common.ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_TENANT_MISMATCH");
                throw new JwtException("Tenant mismatch");
            }

            Long version = JwtService.longClaim(c, "tokenVersion");
            if (version == null || version.intValue() != p.getTokenVersion()) {
                request.setAttribute(com.example.rrms.common.ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_REVOKED");
                throw new JwtException("Token revoked");
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));

        } catch (io.jsonwebtoken.ExpiredJwtException ex) {
            request.setAttribute(com.example.rrms.common.ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_EXPIRED");
            SecurityContextHolder.clearContext();
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            if (request.getAttribute(com.example.rrms.common.ApiErrorWriter.AUTH_ERROR_ATTR) == null) {
                request.setAttribute(com.example.rrms.common.ApiErrorWriter.AUTH_ERROR_ATTR, "TOKEN_INVALID");
            }
            SecurityContextHolder.clearContext();   // leave unauthenticated -> entry point returns 401
        }

        chain.doFilter(request, response);
    }
}
