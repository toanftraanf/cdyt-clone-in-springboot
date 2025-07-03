package com.cdyt.be.common.context;

import com.cdyt.be.entity.User;

/**
 * Thread-local storage for current authenticated user
 * Similar to SecurityContextHolder but for our custom authentication
 */
public class UserContextHolder {

    private static final ThreadLocal<User> userContext = new ThreadLocal<>();

    /**
     * Set the current user for this thread
     */
    public static void setCurrentUser(User user) {
        userContext.set(user);
    }

    /**
     * Get the current user for this thread
     */
    public static User getCurrentUser() {
        return userContext.get();
    }

    /**
     * Clear the current user context for this thread
     */
    public static void clear() {
        userContext.remove();
    }

    /**
     * Check if there is an authenticated user in the current context
     */
    public static boolean hasAuthenticatedUser() {
        return getCurrentUser() != null;
    }
}