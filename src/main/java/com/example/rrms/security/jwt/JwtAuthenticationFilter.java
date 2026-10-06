package com.example.rrms.security.jwt;

import com.example.rrms.security.user.CustomUserDetailsService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.http.HttpHeaders;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   CustomUserDetailsService uds ){
        this.jwtService = jwtService;
        this.userDetailsService = uds;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest reuqest,
                                    HttpServletResponse response,
                                    FilterChain chain) throws
            ServletException , IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if(header == null || !header.startWith("Bearer")) {
            chain.doFilter(request, response);
            return;
        }

        try{
            claims claims =
                    jwtService.parse(header.substring(7));

            //1.only Access tokens authenticate API calls (an
            // MFA token must not work here)
                          if
                          (!jwtService.PURPOSE_ACCESS.equals(claims.get("purpose",
                                  String.class))) {
                              throw new JwtException("Wrong token Purpose");
                          }


            // 2. Load the current truth from the DB
            UserPrincipal principal = userDetailsService.loadUserById(Long.valueOf(claims.getSubject()));

            // 3. Cross-check token vs DB
            if (!principal.isEnabled() || !principal.isAccountNonLocked()) {
                throw new JwtException("Account disabled / tenant suspended / locked");
            }
            Long tokenTenant = JwtService.longClaim(claims, "tenantId");
            if (!Objects.equals(tokenTenant, principal.getTenantId())) {
                throw new JwtException("Tenant mismatch");
            }
            Long tokenVersion = JwtService.longClaim(claims, "tokenVersion");
            if (tokenVersion == null || tokenVersion.intValue() != principal.getTokenVersion()) {
                throw new JwtException("Token revoked");
            }

            // 4. Force password change before anything else
            if (principal.isMustChangePassword()
                    && !request.getRequestURI().equals("/api/auth/change-password")) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"PASSWORD_CHANGE_REQUIRED\"}");
                return;
            }

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException ex) {
            SecurityContextHolder.clearContext();   // leave unauthenticated -> entry point returns 401
        }

        chain.doFilter(request, response);
    }
}

