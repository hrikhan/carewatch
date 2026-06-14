package com.carewatch.features.auth.repository;

import com.carewatch.core.config.DatabaseConfig;
import com.carewatch.features.user.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// DB authentication repository
public class AuthRepository {

    // Authenticate user against database credentials
    public User authenticate(String email, String password) throws SQLException {
        String query = "SELECT user_id, full_name, email, password, role, status, created_at " +
                       "FROM users " +
                       "WHERE email = ? AND password = ?";
                       
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
              
            stmt.setString(1, email);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String status = rs.getString("status");
                    
                    // Only active users can log in
                    if ("ACTIVE".equalsIgnoreCase(status)) {
                        User user = new User();
                        user.setUserId(rs.getInt("user_id"));
                        user.setFullName(rs.getString("full_name"));
                        user.setEmail(rs.getString("email"));
                        user.setPassword(rs.getString("password"));
                        user.setRole(rs.getString("role"));
                        user.setStatus(status);
                        user.setCreatedAt(rs.getTimestamp("created_at"));
                        return user;
                    }
                }
            }
        }
        return null;
    }
}
