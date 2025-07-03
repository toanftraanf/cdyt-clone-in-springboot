package com.cdyt.be.common.cache;

import com.cdyt.be.entity.User;
import com.cdyt.be.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Simple in-memory cache for users to optimize authentication performance
 * Reduces database lookups for frequently authenticated users
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserCache {

    private final UserRepository userRepository;

    // Cache with email as key and CachedUser as value
    private final ConcurrentHashMap<String, CachedUser> cache = new ConcurrentHashMap<>();

    // Cache TTL in milliseconds (5 minutes)
    private static final long CACHE_TTL = TimeUnit.MINUTES.toMillis(5);

    /**
     * Get user from cache or database
     */
    public User getUser(String email) {
        CachedUser cachedUser = cache.get(email);

        // Check if cached and not expired
        if (cachedUser != null && !cachedUser.isExpired()) {
            log.debug("User cache hit for email: {}", email);
            return cachedUser.user;
        }

        // Load from database and cache
        log.debug("User cache miss for email: {}, loading from database", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        // Cache the user
        cache.put(email, new CachedUser(user, System.currentTimeMillis() + CACHE_TTL));

        return user;
    }

    /**
     * Invalidate cache for specific user
     */
    public void evictUser(String email) {
        cache.remove(email);
        log.debug("Evicted user from cache: {}", email);
    }

    /**
     * Clear all cache (useful for testing or admin operations)
     */
    public void clearAll() {
        cache.clear();
        log.debug("Cleared all user cache");
    }

    /**
     * Get cache statistics
     */
    public CacheStats getStats() {
        // Clean expired entries before getting stats
        cleanExpiredEntries();
        return new CacheStats(cache.size());
    }

    /**
     * Clean expired entries (called periodically)
     */
    private void cleanExpiredEntries() {
        long now = System.currentTimeMillis();
        cache.entrySet().removeIf(entry -> entry.getValue().expiresAt <= now);
    }

    /**
     * Cached user wrapper with expiration
     */
    private static class CachedUser {
        final User user;
        final long expiresAt;

        CachedUser(User user, long expiresAt) {
            this.user = user;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAt;
        }
    }

    /**
     * Cache statistics
     */
    public static class CacheStats {
        public final int size;

        CacheStats(int size) {
            this.size = size;
        }

        @Override
        public String toString() {
            return String.format("UserCache{size=%d}", size);
        }
    }
}