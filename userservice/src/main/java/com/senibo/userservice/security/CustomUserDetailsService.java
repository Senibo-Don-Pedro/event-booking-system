package com.senibo.userservice.security;

import com.senibo.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 * Loads user-specific data for authentication from the database.
 * Supports login with either email or username.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Loads user by email or username (used as identifier in login).
     * 
     * @param identifier User's email or username
     * @return UserDetails object containing user information
     * @throws UsernameNotFoundException if user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // 1. Try to parse the identifier as a UUID (This happens during Token Authentication)
        try {
            UUID userId = UUID.fromString(identifier);
            // If it is a valid UUID format, look up by ID
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + identifier));
        } catch (IllegalArgumentException e) {
            // 2. If it throws an error, it's not a UUID. 
            // Treat it as a standard Username or Email (This happens during Login)
            return userRepository.findByEmailOrUsername(identifier, identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));
        }
    }
    // @Override
    // public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
    //     return userRepository.findByEmailOrUsername(identifier, identifier)
    //             .orElseThrow(() -> new UsernameNotFoundException("User not found with identifier: " + identifier));
    // }
}