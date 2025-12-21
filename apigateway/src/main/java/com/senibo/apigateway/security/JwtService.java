package com.senibo.apigateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.function.Function;

import javax.crypto.SecretKey;

/**
 * Service for generating and validating JWT (JSON Web Tokens).
 * Handles token creation, parsing, and validation for authentication.
 */
@Service
public class JwtService {

  @Value("${spring.app.jwtSecret}")
  private String secretKey;

  @Value("${spring.app.jwtExpirationMs}")
  private long jwtExpiration; // Token validity duration in milliseconds

  /**
   * Extracts the username (subject) from the JWT token.
   * 
   * @param token JWT token
   * @return Username stored in the token
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  /**
   * Extracts a specific claim from the JWT token using a claims resolver function.
   * 
   * @param token          JWT token
   * @param claimsResolver Function to extract the desired claim
   * @return Extracted claim value
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }


  /**
     * Validates token by checking expiration and signature.
     * In Gateway, we only care if the token is authentic, not the specific UserDetails object.
     */
    public void validateToken(String token) {
        // This will throw JwtException if invalid or expired
        extractAllClaims(token);
    }

  /**
   * Parses and extracts all claims from the JWT token.
   * 
   * @param token JWT token
   * @return All claims contained in the token
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith((SecretKey) getSignInKey()) // Verify signature with secret key
        .build()
        .parseSignedClaims(token) // Parse the signed token
        .getPayload(); // Extract claims
  }

  /**
   * Generates the signing key from the base64-encoded secret.
   * 
   * @return Signing key for JWT operations
   */
  private Key getSignInKey() {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    return Keys.hmacShaKeyFor(keyBytes);
  }
}