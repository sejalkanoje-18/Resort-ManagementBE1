package com.example.rrms.config;

import com.example.rrms.security.jwt.JwtAuthenticationFilter;
import com.example.rrms.security.user.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

public class SecurityConfig {
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain (HttpSecurity
                                                                http) throws Exception {

        JwtAuthenticationFilter jwtFilter =
                new JwtAuthenticationFilter(jwtService, userDetailsService);

        http.csrf(csrf ->csrf.disable())
         //stateless Bearer-token API
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/mfa/verify", "/api/auth/refresh").permitAll()

                // ---- module (role) gates
                .requestMatchers("/api/platform/**").hasRole("SUPER_ADMIN")
                .requestMatchers("/api/owner/**").hasRole("OWNER")
                .requestMatchers("/api/management/**").hasRole("MANAGEMENT")
                .requestMatchers("/api/staff/**").hasRole("STAFF")
                .requestMatchers("/api/guest/**").hasRole("GUEST")

                // ---- shared endpoints: gated by @PreAuthorize permissions (e.g. POST /api/guests)
                .anyRequest().authenticated()
        )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> writeJson(res, 401, "UNAUTHORIZED"))
                        .accessDeniedHandler((req, res, ex) -> writeJson(res, 403, "FORBIDDEN"))
                )
                .headers(h -> h.frameOptions(f -> f.deny()))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of("https://app.yourdomain.com"));       // set real origins
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }

    private static void writeJson(HttpServletResponse res, int status, String code) throws IOException {
        res.setStatus(status);
        res.setContentType("application/json");
        res.getWriter().write("{\"error\":\"" + code + "\"}");
    }
}
