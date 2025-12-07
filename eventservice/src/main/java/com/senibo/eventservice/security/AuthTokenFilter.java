package com.senibo.eventservice.security;

import java.io.IOException;

import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senibo.eventservice.dto.ApiErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT authentication filter that validates JWT tokens on each request.
 * Extracts user information from valid tokens and sets Spring Security context.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Skip filter for public endpoints
        if (isPublicEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Extract JWT from Authorization header
            String jwt = extractJwtFromRequest(request);

            if (jwt != null) {
                // Extract username (userId) from token
                String userId = jwtService.extractUsername(jwt);

                // If token is valid and no authentication is set
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userId,
                            null,
                            java.util.List.of(new SimpleGrantedAuthority("ROLE_USER"))
                    );

                    // Set authentication in context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            
            // Return 401 Unauthorized with error details
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            
            ApiErrorResponse errorResponse = ApiErrorResponse.of("Invalid or expired token");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    /**
     * Extract JWT token from Authorization header
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Check if endpoint is public (doesn't require authentication)
     * Checks both path and HTTP method
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // Swagger endpoints - always public
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return true;
        }
        
        // Event endpoints - only GET requests are public
        if (method.equals("GET")) {
            if (path.equals("/api/events/published") ||
                path.startsWith("/api/events/search") ||
                path.matches("/api/events/[a-fA-F0-9\\-]+")) {
                return true;
            }
        }
        
        return false;
    }
}