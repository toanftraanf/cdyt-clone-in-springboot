package com.cdyt.be.common.cache;

import com.cdyt.be.entity.Function;
import com.cdyt.be.repository.RoleFunctionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Simple in-memory cache for role permissions (functions) to reduce database
 * lookups during authorization.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RoleCache {

    private final RoleFunctionRepository roleFunctionRepository;

    // Cache key: roleId -> CachedRoleFunctions
    private final ConcurrentHashMap<Integer, CachedRoleFunctions> cache = new ConcurrentHashMap<>();

    // 5-minute TTL similar to UserCache
    private static final long CACHE_TTL = TimeUnit.MINUTES.toMillis(5);

    /**
     * Get list of functions for a single role
     */
    public List<Function> getFunctionsByRoleId(Integer roleId) {
        CachedRoleFunctions cached = cache.get(roleId);
        if (cached != null && !cached.isExpired()) {
            log.debug("RoleCache hit for roleId: {}", roleId);
            return cached.functions;
        }

        log.debug("RoleCache miss for roleId: {}, querying DB", roleId);
        List<Function> functions = roleFunctionRepository.findFunctionsByRoleIds(List.of(roleId));
        cache.put(roleId, new CachedRoleFunctions(functions, System.currentTimeMillis() + CACHE_TTL));
        return functions;
    }

    /**
     * Get combined list of functions for multiple roles (deduplicated)
     */
    public List<Function> getFunctionsByRoleIds(List<Integer> roleIds) {
        List<Function> combined = new ArrayList<>();
        for (Integer roleId : roleIds) {
            combined.addAll(getFunctionsByRoleId(roleId));
        }
        // Optionally deduplicate by id
        Map<Integer, Function> unique = new java.util.LinkedHashMap<>();
        for (Function f : combined) {
            unique.putIfAbsent(f.getId(), f);
        }
        return new ArrayList<>(unique.values());
    }

    /**
     * Evict role from cache
     */
    public void evictRole(Integer roleId) {
        cache.remove(roleId);
    }

    /**
     * Clear cache
     */
    public void clearAll() {
        cache.clear();
    }

    private static class CachedRoleFunctions {
        final List<Function> functions;
        final long expiresAt;

        CachedRoleFunctions(List<Function> functions, long expiresAt) {
            this.functions = functions;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }
}