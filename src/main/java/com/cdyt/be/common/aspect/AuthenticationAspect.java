package com.cdyt.be.common.aspect;

import com.cdyt.be.common.annotation.RequireAuth;
import com.cdyt.be.common.cache.UserCache;
import com.cdyt.be.common.context.UserContextHolder;
import com.cdyt.be.common.controller.BaseAuthController;
import com.cdyt.be.common.dto.ApiResponse;
import com.cdyt.be.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationAspect {

    private final UserCache userCache;

    /**
     * Handle @RequireAuth annotation on controllers and methods
     * Optimized to use Spring Security's authentication with minimal overhead
     */
    @Around("@within(requireAuth) || @annotation(requireAuth)")
    public Object handleAuthentication(ProceedingJoinPoint joinPoint, RequireAuth requireAuth) throws Throwable {

        // Fast authentication check (filter already did validation)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!isAuthenticated(authentication)) {
            return createUnauthorizedResponse();
        }

        // Setup enhanced context only for BaseAuthController
        Object controller = joinPoint.getTarget();
        User currentUser = null;

        if (controller instanceof BaseAuthController baseController) {
            currentUser = getCurrentUserFromAuthentication(authentication);
            setupControllerContext(baseController, currentUser);
        }

        // Set thread-local context for non-BaseAuthController usage
        if (currentUser != null) {
            UserContextHolder.setCurrentUser(currentUser);
        }

        try {
            return joinPoint.proceed();
        } finally {
            UserContextHolder.clear();
        }
    }

    /**
     * Fast authentication check
     */
    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null &&
                authentication.isAuthenticated() &&
                !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * Create standardized unauthorized response
     */
    private ResponseEntity<ApiResponse<Object>> createUnauthorizedResponse() {
        return ResponseEntity.status(401).body(ApiResponse.unauthorized());
    }

    /**
     * Setup controller context efficiently
     */
    private void setupControllerContext(BaseAuthController controller, User currentUser) {
        controller.setCurrentUser(currentUser);
        controller.setAuthResult(BaseAuthController.AuthResult.success());

        // Lazy IP address setup (only when needed)
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes();
        controller.setIpAddress(getClientIpAddress(attributes.getRequest()));
    }

    /**
     * Get current user from Spring Security authentication
     * Uses optimized cache to avoid database lookup on every request
     */
    private User getCurrentUserFromAuthentication(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        return userCache.getUser(email);
    }

    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}