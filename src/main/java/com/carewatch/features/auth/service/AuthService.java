package com.carewatch.features.auth.service;

import com.carewatch.features.auth.repository.AuthRepository;
import com.carewatch.features.user.model.User;
import java.sql.SQLException;
import java.sql.Timestamp;

// Authentication business logic service
public class AuthService {
    private final AuthRepository authRepository = new AuthRepository();

    // Authenticate user, falling back to local demo data if DB is offline
    public User login(String email, String password) throws SQLException, IllegalArgumentException {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        String cleanedEmail = email.trim();

        try {
            // Try DB first
            return authRepository.authenticate(cleanedEmail, password);
        } catch (SQLException dbException) {
            // DB unreachable, try offline fallback
            System.err.println("[CareWatch DB Offline Mode] Database unreachable: " + dbException.getMessage());
            System.err.println("Attempting offline fallback verification...");

            User fallbackUser = authenticateFallback(cleanedEmail, password);
            if (fallbackUser != null) {
                return fallbackUser;
            }
            throw dbException;
        }
    }

    // Local mock data verification for demo fallback
    private User authenticateFallback(String email, String password) {
        if ("123456".equals(password)) {
            if ("patient@gmail.com".equalsIgnoreCase(email)) {
                return new User(101, "Patient User", "patient@gmail.com", "123456", "PATIENT", "ACTIVE", new Timestamp(System.currentTimeMillis()));
            } else if ("doctor@gmail.com".equalsIgnoreCase(email)) {
                return new User(102, "Doctor Admin", "doctor@gmail.com", "123456", "DOCTOR_ADMIN", "ACTIVE", new Timestamp(System.currentTimeMillis()));
            } else if ("admin@gmail.com".equalsIgnoreCase(email)) {
                return new User(103, "Super Admin", "admin@gmail.com", "123456", "SUPER_ADMIN", "ACTIVE", new Timestamp(System.currentTimeMillis()));
            }
        }
        return null;
    }
}
