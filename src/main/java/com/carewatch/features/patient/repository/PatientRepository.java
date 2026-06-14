package com.carewatch.features.patient.repository;

import com.carewatch.core.config.DatabaseConfig;
import com.carewatch.features.patient.model.Patient;
import com.carewatch.features.user.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Database repository for Patient Profiles
public class PatientRepository {

    // Fetch all active users with role PATIENT
    public List<User> findPatientUsersWithoutOrWithProfile() throws SQLException {
        List<User> patients = new ArrayList<>();
        String query = "SELECT user_id, full_name, email FROM users WHERE role = 'PATIENT' AND status = 'ACTIVE' ORDER BY full_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                patients.add(u);
            }
        }
        return patients;
    }

    // Fetch all active users with role DOCTOR_ADMIN
    public List<User> findDoctorAdminUsers() throws SQLException {
        List<User> doctors = new ArrayList<>();
        String query = "SELECT user_id, full_name, email FROM users WHERE role = 'DOCTOR_ADMIN' AND status = 'ACTIVE' ORDER BY full_name";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));
                doctors.add(u);
            }
        }
        return doctors;
    }

    // Insert new patient profile
    public void createPatientProfile(Patient patient) throws SQLException {
        String query = "INSERT INTO patients (user_id, age, gender, phone, address, disease_type, risk_level, assigned_doctor_id, emergency_contact) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patient.getUserId());
            stmt.setInt(2, patient.getAge());
            stmt.setString(3, patient.getGender());
            stmt.setString(4, patient.getPhone());
            stmt.setString(5, patient.getAddress());
            stmt.setString(6, patient.getDiseaseType());
            stmt.setString(7, patient.getRiskLevel());
            
            if (patient.getAssignedDoctorId() > 0) {
                stmt.setInt(8, patient.getAssignedDoctorId());
            } else {
                stmt.setNull(8, java.sql.Types.INTEGER);
            }
            
            stmt.setString(9, patient.getEmergencyContact());
            stmt.executeUpdate();
        }
    }

    // Update existing patient profile
    public void updatePatientProfile(Patient patient) throws SQLException {
        String query = "UPDATE patients SET age = ?, gender = ?, phone = ?, address = ?, disease_type = ?, " +
                       "risk_level = ?, assigned_doctor_id = ?, emergency_contact = ? WHERE patient_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patient.getAge());
            stmt.setString(2, patient.getGender());
            stmt.setString(3, patient.getPhone());
            stmt.setString(4, patient.getAddress());
            stmt.setString(5, patient.getDiseaseType());
            stmt.setString(6, patient.getRiskLevel());
            
            if (patient.getAssignedDoctorId() > 0) {
                stmt.setInt(7, patient.getAssignedDoctorId());
            } else {
                stmt.setNull(7, java.sql.Types.INTEGER);
            }
            
            stmt.setString(8, patient.getEmergencyContact());
            stmt.setInt(9, patient.getPatientId());
            stmt.executeUpdate();
        }
    }

    // Retrieve all patient profiles (with joins)
    public List<Patient> findAllPatientProfiles() throws SQLException {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT p.*, u1.full_name AS patient_name, u1.email AS patient_email, u2.full_name AS doctor_name " +
                       "FROM patients p " +
                       "JOIN users u1 ON p.user_id = u1.user_id " +
                       "LEFT JOIN users u2 ON p.assigned_doctor_id = u2.user_id " +
                       "ORDER BY p.patient_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapPatient(rs));
            }
        }
        return list;
    }

    // Search patient profiles
    public List<Patient> searchPatientProfiles(String keyword) throws SQLException {
        List<Patient> list = new ArrayList<>();
        String query = "SELECT p.*, u1.full_name AS patient_name, u1.email AS patient_email, u2.full_name AS doctor_name " +
                       "FROM patients p " +
                       "JOIN users u1 ON p.user_id = u1.user_id " +
                       "LEFT JOIN users u2 ON p.assigned_doctor_id = u2.user_id " +
                       "WHERE LOWER(u1.full_name) LIKE ? OR LOWER(u1.email) LIKE ? OR LOWER(p.phone) LIKE ? " +
                       "ORDER BY p.patient_id DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            String wildcard = "%" + keyword.toLowerCase() + "%";
            stmt.setString(1, wildcard);
            stmt.setString(2, wildcard);
            stmt.setString(3, wildcard);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapPatient(rs));
                }
            }
        }
        return list;
    }

    // Find patient profile by user id
    public Patient findPatientProfileByUserId(int userId) throws SQLException {
        String query = "SELECT p.*, u1.full_name AS patient_name, u1.email AS patient_email, u2.full_name AS doctor_name " +
                       "FROM patients p " +
                       "JOIN users u1 ON p.user_id = u1.user_id " +
                       "LEFT JOIN users u2 ON p.assigned_doctor_id = u2.user_id " +
                       "WHERE p.user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPatient(rs);
                }
            }
        }
        return null;
    }

    // Map result set to Patient object
    private Patient mapPatient(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setAge(rs.getInt("age"));
        p.setGender(rs.getString("gender"));
        p.setPhone(rs.getString("phone"));
        p.setAddress(rs.getString("address"));
        p.setDiseaseType(rs.getString("disease_type"));
        p.setRiskLevel(rs.getString("risk_level"));
        p.setAssignedDoctorId(rs.getInt("assigned_doctor_id"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        
        // Joined helper attributes
        p.setPatientName(rs.getString("patient_name"));
        p.setPatientEmail(rs.getString("patient_email"));
        p.setAssignedDoctorName(rs.getString("doctor_name") != null ? rs.getString("doctor_name") : "Unassigned");
        
        return p;
    }
}
