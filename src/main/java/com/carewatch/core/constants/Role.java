package com.carewatch.core.constants;

// User roles in CareWatch
public enum Role {
    PATIENT,
    DOCTOR_ADMIN,
    SUPER_ADMIN;

    // Convert string to Role enum
    public static Role fromString(String roleStr) {
        if (roleStr == null) return null;
        try {
            return Role.valueOf(roleStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
