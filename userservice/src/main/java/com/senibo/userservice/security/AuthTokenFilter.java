package com.senibo.userservice.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.senibo.userservice.dto.ApiErrorResponse;

import org.springframework.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT authentication filter that intercepts requests to validate JWT tokens.
 * Executes once per request to extract and validate the JWT from the Authorization header.
 */
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

  private final JwtService jwtService;
  private final UserDetailsService userDetailsService;
  private final ObjectMapper objectMapper;

  /**
   * Filters incoming requests to validate JWT tokens.
   * - Skips authentication for public endpoints (/api/auth/**)
   * - Extracts JWT from Authorization header
   * - Validates token and sets authentication in SecurityContext
   * 
   * @param request     HTTP request
   * @param response    HTTP response
   * @param filterChain Filter chain to continue request processing
   */
  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain) throws ServletException, IOException {

    // Skip JWT validation for public authentication endpoints
    String path = request.getServletPath();

    if (path.startsWith("/api/auth") ||
        path.startsWith("/v3/api-docs") ||
        path.startsWith("/swagger-ui") ||
        path.equals("/swagger-ui.html")) {
      filterChain.doFilter(request, response);
      return;
    }

    final String authHeader = request.getHeader("Authorization");
    final String jwt;
    final String userEmail;

    // Check if Authorization header exists and starts with "Bearer "
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      // Extract JWT token from header (remove "Bearer " prefix)
      jwt = authHeader.substring(7);

      // Extract username from token
      userEmail = jwtService.extractUsername(jwt);

      // If user is found and not already authenticated
      if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        // Load user details from database
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

        // Validate token against user details
        if (jwtService.isTokenValid(jwt, userDetails)) {
          // Create authentication token
          UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
              userDetails,
              null,
              userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

          // Set authentication in SecurityContext
          SecurityContextHolder.getContext().setAuthentication(authToken);
        }
      }

      // Continue with the filter chain
      filterChain.doFilter(request, response);

    } catch (Exception e) {
      // Handle JWT validation errors and return JSON error response
      response.setStatus(HttpStatus.UNAUTHORIZED.value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      ApiErrorResponse error = ApiErrorResponse.of("Invalid or Expired Token", List.of(e.getMessage()));
      objectMapper.writeValue(response.getOutputStream(), error);
    }
  }
}