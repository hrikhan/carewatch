package com.carewatch.features.monitoring.repository;

import com.carewatch.core.config.DatabaseConfig;
import com.carewatch.features.monitoring.model.PatientVitalView;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Repository class handling database monitoring logs for patient vitals
public class MonitoringRepository {

    // Retrieve the latest vitals sign record of all patients matching criteria
    public List<PatientVitalView> findLatestVitals(String searchKeyword, String urgencyFilter) throws SQLException {
        List<PatientVitalView> list = new ArrayList<>();
        
        String query = "SELECT * FROM (" +
                       "    SELECT DISTINCT ON (v.patient_id) " +
                       "        p.patient_id, " +
                       "        u.full_name AS patient_name, " +
                       "        u.email, " +
                       "        p.phone, " +
                       "        p.disease_type, " +
                       "        v.heart_rate, " +
                       "        v.oxygen_level, " +
                       "        v.systolic_bp, " +
                       "        v.diastolic_bp, " +
                       "        v.temperature, " +
                       "        v.sugar_level, " +
                       "        v.symptoms, " +
                       "        v.urgency_level, " +
                       "        v.recorded_at " +
                       "    FROM vitals_history v " +
                       "    JOIN patients p ON v.patient_id = p.patient_id " +
                       "    JOIN users u ON p.user_id = u.user_id " +
                       "    ORDER BY v.patient_id, v.recorded_at DESC" +
                       ") latest_vitals " +
                       "WHERE (LOWER(patient_name) LIKE ? OR LOWER(email) LIKE ? OR LOWER(phone) LIKE ?) " +
                       "  AND (? = 'ALL' OR urgency_level = ?) " +
                       "ORDER BY " +
                       "    CASE " +
                       "        WHEN urgency_level = 'CRITICAL' THEN 1 " +
                       "        WHEN urgency_level = 'WARNING' THEN 2 " +
                       "        WHEN urgency_level = 'NORMAL' THEN 3 " +
                       "        ELSE 4 " +
                       "    END, " +
                       "    recorded_at DESC";

        String keywordPattern = "%";
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            keywordPattern = "%" + searchKeyword.trim().toLowerCase() + "%";
        }

        String filter = (urgencyFilter == null || urgencyFilter.trim().isEmpty()) ? "ALL" : urgencyFilter.trim().toUpperCase();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, keywordPattern);
            stmt.setString(2, keywordPattern);
            stmt.setString(3, keywordPattern);
            stmt.setString(4, filter);
            stmt.setString(5, filter);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PatientVitalView view = new PatientVitalView();
                    view.setPatientId(rs.getInt("patient_id"));
                    view.setPatientName(rs.getString("patient_name"));
                    view.setEmail(rs.getString("email"));
                    view.setPhone(rs.getString("phone"));
                    view.setDiseaseType(rs.getString("disease_type"));
                    view.setHeartRate(rs.getInt("heart_rate"));
                    view.setOxygenLevel(rs.getInt("oxygen_level"));
                    view.setSystolicBp(rs.getInt("systolic_bp"));
                    view.setDiastolicBp(rs.getInt("diastolic_bp"));
                    view.setTemperature(rs.getDouble("temperature"));
                    view.setSugarLevel(rs.getInt("sugar_level"));
                    view.setSymptoms(rs.getString("symptoms"));
                    view.setUrgencyLevel(rs.getString("urgency_level"));
                    view.setRecordedAt(rs.getTimestamp("recorded_at"));
                    
                    list.add(view);
                }
            }
        }
        return list;
    }
}
