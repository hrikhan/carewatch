package com.carewatch.features.vitals.repository;

import com.carewatch.core.config.DatabaseConfig;
import com.carewatch.features.vitals.model.Vital;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// Repository class handling database operations for the Vitals Submit Feature
public class VitalsRepository {

    // Retrieve patient ID by user ID. Returns -1 if no patient profile exists.
    public int findPatientIdByUserId(int userId) throws SQLException {
        String query = "SELECT patient_id FROM patients WHERE user_id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("patient_id");
                }
            }
        }
        return -1;
    }

    // Save vitals entry into database
    public void saveVitals(Vital vital) throws SQLException {
        String query = "INSERT INTO vitals_history (patient_id, heart_rate, oxygen_level, systolic_bp, diastolic_bp, temperature, sugar_level, symptoms, urgency_level) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, vital.getPatientId());
            stmt.setInt(2, vital.getHeartRate());
            stmt.setInt(3, vital.getOxygenLevel());
            stmt.setInt(4, vital.getSystolicBp());
            stmt.setInt(5, vital.getDiastolicBp());
            stmt.setDouble(6, vital.getTemperature());
            stmt.setInt(7, vital.getSugarLevel());
            stmt.setString(8, vital.getSymptoms());
            stmt.setString(9, vital.getUrgencyLevel());
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vital.setVitalId(generatedKeys.getInt(1));
                }
            }
        }
    }

    // Retrieve vitals history for a specific patient (sorted latest first)
    public List<Vital> findVitalsByPatientId(int patientId) throws SQLException {
        List<Vital> list = new ArrayList<>();
        String query = "SELECT * FROM vitals_history WHERE patient_id = ? ORDER BY recorded_at DESC";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapVital(rs));
                }
            }
        }
        return list;
    }

    // Retrieve the most recent vital entry for a patient
    public Vital findLatestVitalsByPatientId(int patientId) throws SQLException {
        String query = "SELECT * FROM vitals_history WHERE patient_id = ? ORDER BY recorded_at DESC LIMIT 1";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapVital(rs);
                }
            }
        }
        return null;
    }

    // Map ResultSet row to Vital model object
    private Vital mapVital(ResultSet rs) throws SQLException {
        Vital v = new Vital();
        v.setVitalId(rs.getInt("vital_id"));
        v.setPatientId(rs.getInt("patient_id"));
        v.setHeartRate(rs.getInt("heart_rate"));
        v.setOxygenLevel(rs.getInt("oxygen_level"));
        v.setSystolicBp(rs.getInt("systolic_bp"));
        v.setDiastolicBp(rs.getInt("diastolic_bp"));
        v.setTemperature(rs.getDouble("temperature"));
        v.setSugarLevel(rs.getInt("sugar_level"));
        v.setSymptoms(rs.getString("symptoms"));
        v.setUrgencyLevel(rs.getString("urgency_level"));
        v.setRecordedAt(rs.getTimestamp("recorded_at"));
        return v;
    }
}
