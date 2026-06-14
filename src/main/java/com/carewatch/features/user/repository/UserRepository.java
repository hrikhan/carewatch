package com.carewatch.features.user.repository;

import com.carewatch.core.config.DatabaseConfig;
import com.carewatch.features.user.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// DB operations for User Management
public class UserRepository {

    // Create a new user in database
    public void createUser(User user, String password) throws SQLException {
        String query = "INSERT INTO users (full_name, email, password, role, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, password);
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getStatus());
            stmt.executeUpdate();
        }
    }

    // Retrieve all users from database
    public List<User> findAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT user_id, full_name, email, password, role, status, created_at FROM users ORDER BY user_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                users.add(mapUser(rs));
            }
        }
        return users;
    }

    // Search users by name/email
    public List<User> searchUsers(String keyword) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT user_id, full_name, email, password, role, status, created_at FROM users " +
                       "WHERE LOWER(full_name) LIKE ? OR LOWER(email) LIKE ? ORDER BY user_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String wildcard = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, wildcard);
            stmt.setString(2, wildcard);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        }
        return users;
    }

    // Filter users by role
    public List<User> filterUsersByRole(String role) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT user_id, full_name, email, password, role, status, created_at FROM users " +
                       "WHERE role = ? ORDER BY user_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, role.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        }
        return users;
    }

    // Update user status
    public void updateUserStatus(int userId, String status) throws SQLException {
        String query = "UPDATE users SET status = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status.toUpperCase());
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    // Map result set to User object
    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        user.setStatus(rs.getString("status"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
