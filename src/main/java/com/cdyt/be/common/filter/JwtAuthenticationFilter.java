package com.cdyt.be.common.filter;

import com.cdyt.be.entity.UserToken;
import com.cdyt.be.repository.AuthRepository;
import com.cdyt.be.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtils jwtUtils;
  private final UserDetailsService userDetailsService;
  private final AuthRepository authRepository;

  // Skip filter for known public paths to improve performance
  private static final String[] PUBLIC_PATHS = {
      "/api/auth/", "/swagger-ui/", "/v3/api-docs/", "/webjars/", "/actuator/health"
  };

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    // Skip filter if no Authorization header and it's a likely public endpoint
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null) {
      return java.util.Arrays.stream(PUBLIC_PATHS)
          .anyMatch(path::startsWith);
    }
    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    String token = getTokenFromRequest(request);

    // Only process if token exists and is valid in database
    if (token != null && isTokenValid(token)) {
      // Avoid duplicate authentication setting
      if (SecurityContextHolder.getContext().getAuthentication() == null) {
        try {
          String email = jwtUtils.extractUsername(token);
          UserDetails userDetails = userDetailsService.loadUserByUsername(email);

          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
              null,
              userDetails.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
          log.error("Failed to extract username from token or load user details: ", e);
          // Token might be malformed even though it exists in database
        }
      }
    }

    filterChain.doFilter(request, response);
  }

  private String getTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }

  /**
   * Database-only token validation: checks ONLY database token existence and
   * expiration
   * Ignores JWT internal expiration - database controls token lifetime
   */
  private boolean isTokenValid(String token) {
    try {
      // Only check database token expiration (ignore JWT internal expiration)
      // Use JOIN FETCH to eagerly load user and avoid LazyInitializationException
      UserToken userToken = authRepository.findByTokenWithUser(token).orElse(null);

      if (userToken == null) {
        log.debug("Token not found in database: {}", token.substring(0, Math.min(10, token.length())) + "...");
        return false;
      }

      if (userToken.getExpiredDate().isBefore(LocalDateTime.now())) {
        log.debug("Database token expired for user: {}", userToken.getUser().getEmail());
        // Clean up expired token
        authRepository.delete(userToken);
        return false;
      }

      log.debug("Token validation successful for user: {}", userToken.getUser().getEmail());
      return true;

    } catch (Exception e) {
      log.error("Token validation error: ", e);
      return false;
    }
  }
}