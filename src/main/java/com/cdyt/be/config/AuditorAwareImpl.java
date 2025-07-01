package com.cdyt.be.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

  @Override
  public Optional<String> getCurrentAuditor() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated() ||
        "anonymousUser".equals(authentication.getPrincipal())) {
      return Optional.of("SYSTEM"); // Default user when no authentication
    }

    // Return the username from the authentication
    return Optional.of(authentication.getName());
  }
}
