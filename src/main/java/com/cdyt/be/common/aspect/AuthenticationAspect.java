package com.cdyt.be.common.aspect;

import com.cdyt.be.common.annotation.RequireAuth;
import com.cdyt.be.common.cache.UserCache;
import com.cdyt.be.common.cache.RoleCache;
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

import java.util.List;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationAspect {

    private final UserCache userCache;
    private final com.cdyt.be.repository.FunctionRepository functionRepository;
    private final com.cdyt.be.common.cache.RoleCache roleCache;

    /**
     * Handle @RequireAuth annotation on controllers and methods Optimized to use
     * Spring Security's
     * authentication with minimal overhead
     */
    @Around("@within(requireAuth) || @annotation(requireAuth)")
    public Object handleAuthentication(ProceedingJoinPoint joinPoint, RequireAuth requireAuth)
            throws Throwable {

        // Get annotation from class if not present at method level
        if (requireAuth == null) {
            requireAuth = joinPoint.getTarget().getClass().getAnnotation(RequireAuth.class);
        }

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

        // ===== Permission check based on RequireAuth settings =====
        if (requireAuth != null && requireAuth.checkPermissions()) {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                    .getRequest();
            String apiPath = request.getRequestURI();

            boolean permitted = checkPermission(currentUser, apiPath);
            if (!permitted) {
                return createForbiddenResponse("You don't have permission to access this resource.");
            }
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
     * Create standardized forbidden response
     */
    private ResponseEntity<ApiResponse<Object>> createForbiddenResponse(String message) {
        return ResponseEntity.status(403).body(ApiResponse.forbidden(message));
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
     * Get current user from Spring Security authentication Uses optimized cache to
     * avoid database
     * lookup on every request
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

    // ====================== PERMISSION LOGIC =======================

    /**
     * Check whether current user has permission to access given API path Logic
     * adapted from the
     * provided C# AuthenPermissionAttribute code.
     */
    private boolean checkPermission(User user, String apiPath) {
        try {
            System.out.println("Checking permission for API path: " + apiPath);
            // If API is not in global function list => no permission needed
            List<com.cdyt.be.entity.Function> allFunctions = functionRepository.findAll();
            boolean pathRequiresPermission = allFunctions.stream()
                    .filter(f -> Boolean.FALSE.equals(f.getIsDelete()))
                    .anyMatch(f -> apiPath.startsWith(f.getApiUrl()));

            if (!pathRequiresPermission) {
                return true; // Not a protected API
            }

            if (user == null || user.getRole() == null || user.getRole().isEmpty()) {
                return false; // No roles
            }

            List<Integer> roleIds = user.getRole().stream().map(com.cdyt.be.entity.Role::getId).toList();
            List<com.cdyt.be.entity.Function> userFunctions = roleCache.getFunctionsByRoleIds(roleIds);

            if (userFunctions == null || userFunctions.isEmpty()) {
                return false; // User has no function permissions
            }

            // User is permitted if any function path matches exactly or as prefix
            return userFunctions.stream()
                    .anyMatch(f -> apiPath.startsWith(f.getApiUrl()));
        } catch (Exception ex) {
            log.error("Error checking permission", ex);
            return false;
        }
    }
}