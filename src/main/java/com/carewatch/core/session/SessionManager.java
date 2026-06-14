package com.carewatch.core.session;

import com.carewatch.features.user.model.User;

// Manage logged-in user session
public class SessionManager {
    private static User currentUser;

    // Start session
    public static void startSession(User user) {
        currentUser = user;
    }

    // Get current user
    public static User getCurrentUser() {
        return currentUser;
    }

    // Clear session
    public static void clearSession() {
        currentUser = null;
    }

    // Check if user is logged in
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    private static String pendingFragment;

    public static String getPendingFragment() {
        return pendingFragment;
    }

    public static void setPendingFragment(String fragment) {
        pendingFragment = fragment;
    }
}
