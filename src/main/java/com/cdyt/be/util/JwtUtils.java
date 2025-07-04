package com.cdyt.be.util;

import com.cdyt.be.entity.Role;
import com.cdyt.be.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class JwtUtils {

  @Value("${jwt.secret}")
  private String jwtSecret;

  // Set JWT expiration longer than database expiration since database controls
  // real expiration
  // This prevents JWT parsing errors while database handles the actual token
  // lifecycle
  @Value("${jwt.expiration-days:30}") // Default 30 days if not specified
  private int jwtInternalExpirationDays;

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  /**
   * Generate JWT token with longer expiration than database token.
   * Database controls actual token lifecycle, JWT expiration is just for token
   * integrity.
   */
  public String generateToken(User user) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", user.getRole().stream().map(Role::getRoleName).toList());
    claims.put("userId", user.getId());
    claims.put("isActive", user.getIsActive());

    // Set JWT expiration longer than max database expiration to avoid parsing
    // issues
    long jwtExpirationMs = (long) jwtInternalExpirationDays * 24 * 60 * 60 * 1000;

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(user.getEmail())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
        .signWith(getSigningKey())
        .compact();
  }

  /**
   * Extract username/email from JWT token
   */
  public String extractUsername(String token) {
    return extractClaims(token).getSubject();
  }

  /**
   * Extract user ID from JWT token (for optimization)
   */
  public Long extractUserId(String token) {
    return Long.valueOf(extractClaims(token).get("userId").toString());
  }

  /**
   * Extract user roles from JWT token
   */
  @SuppressWarnings("unchecked")
  public List<String> extractRoles(String token) {
    return (List<String>) extractClaims(token).get("roles");
  }

  /**
   * Extract all claims from JWT token
   */
  public Claims extractClaims(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(getSigningKey())
        .build()
        .parseClaimsJws(token)
        .getBody();
  }

  /**
   * Validate JWT token structure and signature (not expiration - database handles
   * that)
   */
  public boolean isTokenStructureValid(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(getSigningKey())
          .build()
          .parseClaimsJws(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  /**
   * Legacy method for backward compatibility
   * 
   * @deprecated Use isTokenStructureValid() instead
   */
  @Deprecated
  public boolean validateToken(String token) {
    return isTokenStructureValid(token);
  }
}
