package com.example.rrms.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity
                                                           http) throws Exception {

        http.csrf(c -> c.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/auth/login",
                                "/api/auth/mfa/verify",
                                "/api/auth/refresh")
                        .permitAll()

                        // ---- module (role) gates
                        .requestMatchers("/api/platform/**")
                        .hasRole("SUPER_ADMIN")

                        .requestMatchers("/api/owner/**")
                        .hasRole("OWNER")

                        .requestMatchers("/api/management/**")
                        .hasRole("MANAGEMENT")

                        .requestMatchers("/api/staff/**")
                        .hasRole("STAFF")

                        .requestMatchers("/api/guest/**")
                        .hasRole("GUEST")


                        // ---- shared endpoints: gated by @PreAuthorize permissions (e.g. POST /api/guests)
                        .anyRequest().authenticated());

        return http.build();
    }


}