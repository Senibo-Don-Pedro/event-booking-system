package com.senibo.eventservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

// You can keep all the methods, but Event Service mainly needs:
// - `extractUsername(String token)` - Get username from JWT
// - `isTokenValid(String token, UserDetails userDetails)` - Validate JWT
// - `extractClaim(String token, Function<Claims, T> claimsResolver)` - Extract any claim



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
   * Generates a JWT token for the given user without extra claims.
   * 
   * @param userDetails User details
   * @return Generated JWT token
   */
  public String generateToken(UserDetails userDetails) {
    return generateToken(new HashMap<>(), userDetails);
  }

  /**
   * Generates a JWT token with custom claims for the given user.
   * Token includes: claims, subject (username), issued time, and expiration time.
   * 
   * @param extraClaims Additional claims to include in the token
   * @param userDetails User details
   * @return Generated JWT token
   */
  public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return Jwts.builder()
        .claims(extraClaims) // Add custom claims
        .subject(userDetails.getUsername()) // Set username as subject
        .issuedAt(new Date(System.currentTimeMillis())) // Token creation time
        .expiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Token expiry time
        .signWith(getSignInKey()) // Sign with secret key
        .compact();
  }

  /**
   * Validates if the token belongs to the given user and is not expired.
   * 
   * @param token       JWT token
   * @param userDetails User details to validate against
   * @return true if token is valid, false otherwise
   */
  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  /**
   * Checks if the token has expired.
   * 
   * @param token JWT token
   * @return true if token is expired, false otherwise
   */
  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  /**
   * Extracts the expiration date from the token.
   * 
   * @param token JWT token
   * @return Expiration date
   */
  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
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