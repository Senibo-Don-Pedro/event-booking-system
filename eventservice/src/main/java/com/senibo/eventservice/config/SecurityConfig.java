package com.senibo.eventservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.senibo.eventservice.dto.ApiErrorResponse;
import com.senibo.eventservice.security.AuthTokenFilter;

import lombok.RequiredArgsConstructor;

/**
 * Security configuration for Event Service.
 * Configures JWT-based authentication for protected endpoints.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthTokenFilter authTokenFilter;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Disable CSRF for stateless API
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // No sessions
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/v3/api-docs/**", "/swagger", "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/published").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/events/{id}").permitAll()
                        // ✅ ADD THIS LINE: Allow PATCH /tickets without JWT
                        .requestMatchers(HttpMethod.PATCH, "/api/events/*/tickets").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationEntryPoint()))
                .addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Custom entry point for authentication failures.
     * Returns 401 Unauthorized with proper error message.
     */
    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");

            ApiErrorResponse errorResponse = ApiErrorResponse.of(
                    "Authentication required. Please provide a valid JWT token in the Authorization header.");

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }
}