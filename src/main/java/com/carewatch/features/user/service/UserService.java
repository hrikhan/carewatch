package com.carewatch.features.user.service;

import com.carewatch.features.user.model.User;
import com.carewatch.features.user.repository.UserRepository;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

// Service layer for user validation and CRUD logic
public class UserService {
    private final UserRepository userRepository = new UserRepository();

    // Simple email regex pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    // Validate and create new user
    public void createUser(User user, String password) throws SQLException, IllegalArgumentException {
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full Name cannot be empty.");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new IllegalArgumentException("Invalid email format.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("Role must be specified.");
        }
        if (user.getStatus() == null || user.getStatus().trim().isEmpty()) {
            throw new IllegalArgumentException("Status must be specified.");
        }

        user.setFullName(user.getFullName().trim());
        user.setEmail(user.getEmail().trim().toLowerCase());
        
        userRepository.createUser(user, password);
    }

    // Get all users
    public List<User> getAllUsers() throws SQLException {
        return userRepository.findAllUsers();
    }

    // Search users by keyword
    public List<User> searchUsers(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.searchUsers(keyword.trim());
    }

    // Filter users by role
    public List<User> filterUsersByRole(String role) throws SQLException {
        if (role == null || role.trim().isEmpty() || "ALL".equalsIgnoreCase(role)) {
            return getAllUsers();
        }
        return userRepository.filterUsersByRole(role.trim());
    }

    // Update user status
    public void updateUserStatus(int userId, String status) throws SQLException, IllegalArgumentException {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be empty.");
        }
        String upperStatus = status.trim().toUpperCase();
        if (!"ACTIVE".equals(upperStatus) && !"INACTIVE".equals(upperStatus)) {
            throw new IllegalArgumentException("Status must be ACTIVE or INACTIVE.");
        }
        userRepository.updateUserStatus(userId, upperStatus);
    }
}
