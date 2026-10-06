package com.example.rrms.security.jwt;

import com.example.rrms.security.user.CustomUserDetailsService;
import com.example.rrms.security.user.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.security.SecurityUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.http.HttpHeaders;
import java.util.Objects;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if(header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try{
                Claims c =
                    jwtService.parse(header.substring(7));

            //1.only Access tokens authenticate API calls (an
            // MFA token must not work here)
                          if
                          (!jwtService.PURPOSE_ACCESS.equals(c.get("purpose",
                                  String.class))) {
                              throw new JwtException("Wrong token Purpose");
                          }


            // 2. Load the current truth from the DB
            UserPrincipal p = userDetailsService.loadUserById(Long.valueOf(c.getSubject()));

            // 3. Cross-check token vs DB
            if (!p.isEnabled() || !p.isAccountNonLocked()) {
                throw new JwtException("Account disabled / tenant suspended / locked");
            }
            Long tokenTenant = ((Number) c.get("tenantId"))
                    .longValue();

            if (!Objects.equals(
                    tokenTenant, p.getTenantId())) {
                throw new JwtException("Tenant mismatch");
            }

            Long version = ((Number) c.get("tokenVersion"))
                    .longValue();

            if (version.intValue()!= p.getTokenVersion()) {
                throw new JwtException("Token revoked");
            }

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(
                            new UsernamePasswordAuthenticationToken(
                                    p, null, p.getAuthorities()));

        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();   // leave unauthenticated -> entry point returns 401
        }

        chain.doFilter(request, response);
    }
}

