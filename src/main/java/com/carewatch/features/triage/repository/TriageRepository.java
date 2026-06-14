package com.carewatch.features.triage.repository;

import com.carewatch.core.config.DatabaseConfig;
import com.carewatch.features.triage.model.TriageItem;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Repository class handling database logs for triage queues
public class TriageRepository {

    // Insert or update patient's pending triage item. If an item is already PENDING, updates it.
    public void createOrUpdateTriageItem(TriageItem item) throws SQLException {
        String checkQuery = "SELECT queue_id FROM triage_queue WHERE patient_id = ? AND status = 'PENDING'";
        int existingQueueId = -1;

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setInt(1, item.getPatientId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    existingQueueId = rs.getInt("queue_id");
                }
            }
        }

        if (existingQueueId != -1) {
            // Update existing pending triage item
            String updateQuery = "UPDATE triage_queue " +
                                 "SET vital_id = ?, urgency_level = ?, priority_score = ?, alert_message = ?, created_at = CURRENT_TIMESTAMP " +
                                 "WHERE queue_id = ?";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setInt(1, item.getVitalId());
                updateStmt.setString(2, item.getUrgencyLevel());
                updateStmt.setInt(3, item.getPriorityScore());
                updateStmt.setString(4, item.getAlertMessage());
                updateStmt.setInt(5, existingQueueId);
                updateStmt.executeUpdate();
            }
        } else {
            // Insert a new triage queue entry
            String insertQuery = "INSERT INTO triage_queue (patient_id, vital_id, urgency_level, priority_score, alert_message, status) " +
                                 "VALUES (?, ?, ?, ?, ?, 'PENDING')";
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setInt(1, item.getPatientId());
                insertStmt.setInt(2, item.getVitalId());
                insertStmt.setString(3, item.getUrgencyLevel());
                insertStmt.setInt(4, item.getPriorityScore());
                insertStmt.setString(5, item.getAlertMessage());
                insertStmt.executeUpdate();
            }
        }
    }

    // Retrieve all pending queue items
    public List<TriageItem> findPendingQueue() throws SQLException {
        List<TriageItem> list = new ArrayList<>();
        String query = "SELECT t.*, u.full_name AS patient_name, u.email, p.phone, p.disease_type " +
                       "FROM triage_queue t " +
                       "JOIN patients p ON t.patient_id = p.patient_id " +
                       "JOIN users u ON p.user_id = u.user_id " +
                       "WHERE t.status = 'PENDING' " +
                       "ORDER BY t.priority_score ASC, t.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapTriageItem(rs));
            }
        }
        return list;
    }

    // Retrieve all triage queue items
    public List<TriageItem> findAllQueue() throws SQLException {
        List<TriageItem> list = new ArrayList<>();
        String query = "SELECT t.*, u.full_name AS patient_name, u.email, p.phone, p.disease_type " +
                       "FROM triage_queue t " +
                       "JOIN patients p ON t.patient_id = p.patient_id " +
                       "JOIN users u ON p.user_id = u.user_id " +
                       "ORDER BY " +
                       "    CASE WHEN t.status = 'PENDING' THEN 1 ELSE 2 END, " +
                       "    t.priority_score ASC, " +
                       "    t.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapTriageItem(rs));
            }
        }
        return list;
    }

    // Search queue by patient name or email
    public List<TriageItem> searchQueue(String keyword) throws SQLException {
        List<TriageItem> list = new ArrayList<>();
        String query = "SELECT t.*, u.full_name AS patient_name, u.email, p.phone, p.disease_type " +
                       "FROM triage_queue t " +
                       "JOIN patients p ON t.patient_id = p.patient_id " +
                       "JOIN users u ON p.user_id = u.user_id " +
                       "WHERE LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ? " +
                       "ORDER BY " +
                       "    CASE WHEN t.status = 'PENDING' THEN 1 ELSE 2 END, " +
                       "    t.priority_score ASC, " +
                       "    t.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTriageItem(rs));
                }
            }
        }
        return list;
    }

    // Filter queue by status (ALL, PENDING, RESOLVED)
    public List<TriageItem> filterQueueByStatus(String status) throws SQLException {
        if ("ALL".equalsIgnoreCase(status)) {
            return findAllQueue();
        }
        
        List<TriageItem> list = new ArrayList<>();
        String query = "SELECT t.*, u.full_name AS patient_name, u.email, p.phone, p.disease_type " +
                       "FROM triage_queue t " +
                       "JOIN patients p ON t.patient_id = p.patient_id " +
                       "JOIN users u ON p.user_id = u.user_id " +
                       "WHERE LOWER(t.status) = LOWER(?) " +
                       "ORDER BY " +
                       "    CASE WHEN t.status = 'PENDING' THEN 1 ELSE 2 END, " +
                       "    t.priority_score ASC, " +
                       "    t.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTriageItem(rs));
                }
            }
        }
        return list;
    }

    // Combined search and status filtering (highly useful for real-time dashboard UI updates)
    public List<TriageItem> searchAndFilterQueue(String keyword, String status) throws SQLException {
        List<TriageItem> list = new ArrayList<>();
        String query = "SELECT t.*, u.full_name AS patient_name, u.email, p.phone, p.disease_type " +
                       "FROM triage_queue t " +
                       "JOIN patients p ON t.patient_id = p.patient_id " +
                       "JOIN users u ON p.user_id = u.user_id " +
                       "WHERE (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ?) " +
                       "  AND (? = 'ALL' OR LOWER(t.status) = LOWER(?)) " +
                       "ORDER BY " +
                       "    CASE WHEN t.status = 'PENDING' THEN 1 ELSE 2 END, " +
                       "    t.priority_score ASC, " +
                       "    t.created_at DESC";
        
        String keywordPattern = "%";
        if (keyword != null && !keyword.trim().isEmpty()) {
            keywordPattern = "%" + keyword.trim().toLowerCase() + "%";
        }
        
        String filter = (status == null || status.trim().isEmpty()) ? "ALL" : status.trim();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, keywordPattern);
            stmt.setString(2, keywordPattern);
            stmt.setString(3, filter);
            stmt.setString(4, filter);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTriageItem(rs));
                }
            }
        }
        return list;
    }

    // Mark triage queue entry as resolved
    public void markAsResolved(int queueId) throws SQLException {
        String query = "UPDATE triage_queue SET status = 'RESOLVED' WHERE queue_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, queueId);
            stmt.executeUpdate();
        }
    }

    // Map ResultSet row to TriageItem model
    private TriageItem mapTriageItem(ResultSet rs) throws SQLException {
        TriageItem t = new TriageItem();
        t.setQueueId(rs.getInt("queue_id"));
        t.setPatientId(rs.getInt("patient_id"));
        t.setVitalId(rs.getInt("vital_id"));
        t.setUrgencyLevel(rs.getString("urgency_level"));
        t.setPriorityScore(rs.getInt("priority_score"));
        t.setAlertMessage(rs.getString("alert_message"));
        t.setStatus(rs.getString("status"));
        t.setCreatedAt(rs.getTimestamp("created_at"));
        
        // Joined details
        t.setPatientName(rs.getString("patient_name"));
        t.setEmail(rs.getString("email"));
        t.setPhone(rs.getString("phone"));
        t.setDiseaseType(rs.getString("disease_type"));
        
        return t;
    }
}
