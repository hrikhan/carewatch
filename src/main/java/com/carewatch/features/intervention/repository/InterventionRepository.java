package com.carewatch.features.intervention.repository;

import com.carewatch.core.config.DatabaseConfig;
import com.carewatch.features.intervention.model.DoctorIntervention;
import com.carewatch.features.patient.model.Patient;
import com.carewatch.features.user.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Repository class handling database logs for doctor medical interventions
public class InterventionRepository {

    // Save medical action note
    public void saveIntervention(DoctorIntervention intervention) throws SQLException {
        String query = "INSERT INTO doctor_interventions (patient_id, doctor_user_id, action_taken, notes) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, intervention.getPatientId());
            stmt.setInt(2, intervention.getDoctorUserId());
            stmt.setString(3, intervention.getActionTaken());
            stmt.setString(4, intervention.getNotes());
            stmt.executeUpdate();
        }
    }

    // Retrieve all recorded interventions
    public List<DoctorIntervention> findAllInterventions() throws SQLException {
        List<DoctorIntervention> list = new ArrayList<>();
        String query = "SELECT di.*, u1.full_name AS patient_name, u2.full_name AS doctor_name " +
                       "FROM doctor_interventions di " +
                       "JOIN patients p ON di.patient_id = p.patient_id " +
                       "JOIN users u1 ON p.user_id = u1.user_id " +
                       "JOIN users u2 ON di.doctor_user_id = u2.user_id " +
                       "ORDER BY di.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapIntervention(rs));
            }
        }
        return list;
    }

    // Search interventions by patient name or email
    public List<DoctorIntervention> searchInterventions(String keyword) throws SQLException {
        List<DoctorIntervention> list = new ArrayList<>();
        String query = "SELECT di.*, u1.full_name AS patient_name, u2.full_name AS doctor_name " +
                       "FROM doctor_interventions di " +
                       "JOIN patients p ON di.patient_id = p.patient_id " +
                       "JOIN users u1 ON p.user_id = u1.user_id " +
                       "JOIN users u2 ON di.doctor_user_id = u2.user_id " +
                       "WHERE LOWER(u1.full_name) LIKE ? OR LOWER(u1.email) LIKE ? " +
                       "ORDER BY di.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIntervention(rs));
                }
            }
        }
        return list;
    }

    // Retrieve interventions associated with a specific patient
    public List<DoctorIntervention> findInterventionsByPatientId(int patientId) throws SQLException {
        List<DoctorIntervention> list = new ArrayList<>();
        String query = "SELECT di.*, u1.full_name AS patient_name, u2.full_name AS doctor_name " +
                       "FROM doctor_interventions di " +
                       "JOIN patients p ON di.patient_id = p.patient_id " +
                       "JOIN users u1 ON p.user_id = u1.user_id " +
                       "JOIN users u2 ON di.doctor_user_id = u2.user_id " +
                       "WHERE di.patient_id = ? " +
                       "ORDER BY di.created_at DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIntervention(rs));
                }
            }
        }
        return list;
    }

    // Fetch active doctor/admin users
    public List<User> findDoctorAdminUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String query = "SELECT user_id, full_name, email, role FROM users " +
                       "WHERE role IN ('DOCTOR_ADMIN', 'SUPER_ADMIN') AND status = 'ACTIVE' " +
                       "ORDER BY full_name";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                u.setRole(rs.getString("role"));
                list.add(u);
            }
        }
        return list;
    }

    // Fetch patient list to choose from in form drop-down
    public List<Patient> findPatientsFromTriageOrAllPatients() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT p.patient_id, p.user_id, u.full_name, u.email " +
                       "FROM patients p " +
                       "JOIN users u ON p.user_id = u.user_id " +
                       "WHERE u.status = 'ACTIVE' " +
                       "ORDER BY u.full_name";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getInt("patient_id"));
                p.setUserId(rs.getInt("user_id"));
                p.setPatientName(rs.getString("full_name"));
                p.setPatientEmail(rs.getString("email"));
                list.add(p);
            }
        }
        return list;
    }

    // Combined search and filtering logic
    public List<DoctorIntervention> searchAndFilterInterventions(String keyword, int doctorUserId) throws SQLException {
        List<DoctorIntervention> list = new ArrayList<>();
        String query = "SELECT di.*, u1.full_name AS patient_name, u2.full_name AS doctor_name " +
                       "FROM doctor_interventions di " +
                       "JOIN patients p ON di.patient_id = p.patient_id " +
                       "JOIN users u1 ON p.user_id = u1.user_id " +
                       "JOIN users u2 ON di.doctor_user_id = u2.user_id " +
                       "WHERE (LOWER(u1.full_name) LIKE ? OR LOWER(u1.email) LIKE ?) " +
                       "  AND (? = 0 OR di.doctor_user_id = ?) " +
                       "ORDER BY di.created_at DESC";

        String keywordPattern = "%";
        if (keyword != null && !keyword.trim().isEmpty()) {
            keywordPattern = "%" + keyword.trim().toLowerCase() + "%";
        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, keywordPattern);
            stmt.setString(2, keywordPattern);
            stmt.setInt(3, doctorUserId);
            stmt.setInt(4, doctorUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapIntervention(rs));
                }
            }
        }
        return list;
    }

    // Map ResultSet row to DoctorIntervention
    private DoctorIntervention mapIntervention(ResultSet rs) throws SQLException {
        DoctorIntervention d = new DoctorIntervention();
        d.setInterventionId(rs.getInt("intervention_id"));
        d.setPatientId(rs.getInt("patient_id"));
        d.setDoctorUserId(rs.getInt("doctor_user_id"));
        d.setActionTaken(rs.getString("action_taken"));
        d.setNotes(rs.getString("notes"));
        d.setCreatedAt(rs.getTimestamp("created_at"));
        
        // Joined details
        d.setPatientName(rs.getString("patient_name"));
        d.setDoctorName(rs.getString("doctor_name"));
        return d;
    }
}
