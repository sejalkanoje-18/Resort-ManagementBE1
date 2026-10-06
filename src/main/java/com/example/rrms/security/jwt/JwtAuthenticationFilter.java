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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            Claims c = jwtService.parse(header.substring(7));

            // 1. Only Access tokens authenticate API calls
            if (!JwtService.PURPOSE_ACCESS.equals(c.get("purpose", String.class))) {
                throw new JwtException("Wrong token purpose");
            }

            // 2. Load the current user state from DB
            UserPrincipal p = userDetailsService.loadUserById(Long.valueOf(c.getSubject()));

            // 3. Cross-check token vs DB
            if (!p.isEnabled() || !p.isAccountNonLocked()) {
                throw new JwtException("Account disabled, tenant suspended, or account locked");
            }

            Long tokenTenant = JwtService.longClaim(c, "tenantId");
            if (!Objects.equals(tokenTenant, p.getTenantId())) {
                throw new JwtException("Tenant mismatch");
            }

            Long version = JwtService.longClaim(c, "tokenVersion");
            if (version == null || version.intValue() != p.getTokenVersion()) {
                throw new JwtException("Token revoked");
            }

            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));

        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();   // leave unauthenticated -> entry point returns 401
        }

        chain.doFilter(request, response);
    }
}
