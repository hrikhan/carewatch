package com.carewatch.features.report.repository;

import com.carewatch.core.config.DatabaseConfig;
import com.carewatch.features.report.model.ReportRow;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Repository class handling database operations for the Reports & Export Feature
public class ReportRepository {

    // Retrieve clinical summary metric counts
    public Map<String, Integer> getSummaryCounts() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        
        String query = "SELECT " +
                       "(SELECT COUNT(*) FROM patients) AS total_patients, " +
                       "(SELECT COUNT(*) FROM vitals_history) AS total_vitals, " +
                       "(SELECT COUNT(*) FROM vitals_history WHERE urgency_level = 'CRITICAL') AS critical_cases, " +
                       "(SELECT COUNT(*) FROM vitals_history WHERE urgency_level = 'WARNING') AS warning_cases, " +
                       "(SELECT COUNT(*) FROM triage_queue WHERE status = 'RESOLVED') AS resolved_cases, " +
                       "(SELECT COUNT(*) FROM doctor_interventions) AS total_interventions";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                counts.put("Total Patients", rs.getInt("total_patients"));
                counts.put("Total Vitals Submitted", rs.getInt("total_vitals"));
                counts.put("Critical Cases", rs.getInt("critical_cases"));
                counts.put("Warning Cases", rs.getInt("warning_cases"));
                counts.put("Resolved Triage Cases", rs.getInt("resolved_cases"));
                counts.put("Total Doctor Interventions", rs.getInt("total_interventions"));
            }
        }
        return counts;
    }

    // Retrieve all report logs
    public List<ReportRow> findReportRows() throws SQLException {
        return searchAndFilterReports(null, "ALL", null, null);
    }

    // Search report logs by patient name or email
    public List<ReportRow> searchReportRows(String keyword) throws SQLException {
        return searchAndFilterReports(keyword, "ALL", null, null);
    }

    // Filter report logs by urgency status and date range
    public List<ReportRow> filterReportRows(String urgency, LocalDate startDate, LocalDate endDate) throws SQLException {
        return searchAndFilterReports(null, urgency, startDate, endDate);
    }

    // Unified helper query method to perform search and filtering
    public List<ReportRow> searchAndFilterReports(String keyword, String urgency, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<ReportRow> list = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder(
            "SELECT u.full_name AS patient_name, u.email, p.phone, p.disease_type, " +
            "v.heart_rate, v.oxygen_level, v.systolic_bp, v.diastolic_bp, v.temperature, v.sugar_level, v.urgency_level, " +
            "COALESCE(t.status, 'NO ALERT') AS triage_status, " +
            "COALESCE((SELECT action_taken FROM doctor_interventions WHERE patient_id = v.patient_id ORDER BY created_at DESC LIMIT 1), 'None') AS last_intervention, " +
            "v.recorded_at " +
            "FROM vitals_history v " +
            "JOIN patients p ON v.patient_id = p.patient_id " +
            "JOIN users u ON p.user_id = u.user_id " +
            "LEFT JOIN triage_queue t ON v.vital_id = t.vital_id " +
            "WHERE 1=1"
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(u.full_name) LIKE ? OR LOWER(u.email) LIKE ?)");
            String pattern = "%" + keyword.trim().toLowerCase() + "%";
            params.add(pattern);
            params.add(pattern);
        }

        if (urgency != null && !"ALL".equalsIgnoreCase(urgency)) {
            sql.append(" AND v.urgency_level = ?");
            params.add(urgency.toUpperCase());
        }

        if (startDate != null) {
            sql.append(" AND v.recorded_at >= ?");
            params.add(Timestamp.valueOf(startDate.atStartOfDay()));
        }

        if (endDate != null) {
            sql.append(" AND v.recorded_at <= ?");
            params.add(Timestamp.valueOf(endDate.atTime(23, 59, 59)));
        }

        sql.append(" ORDER BY v.recorded_at DESC");

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) {
                    stmt.setString(i + 1, (String) p);
                } else if (p instanceof Timestamp) {
                    stmt.setTimestamp(i + 1, (Timestamp) p);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ReportRow row = new ReportRow();
                    row.setPatientName(rs.getString("patient_name"));
                    row.setEmail(rs.getString("email"));
                    row.setPhone(rs.getString("phone"));
                    row.setDiseaseType(rs.getString("disease_type"));
                    row.setHeartRate(rs.getInt("heart_rate"));
                    row.setOxygenLevel(rs.getInt("oxygen_level"));
                    row.setSystolicBp(rs.getInt("systolic_bp"));
                    row.setDiastolicBp(rs.getInt("diastolic_bp"));
                    row.setTemperature(rs.getDouble("temperature"));
                    row.setSugarLevel(rs.getInt("sugar_level"));
                    row.setUrgencyLevel(rs.getString("urgency_level"));
                    row.setTriageStatus(rs.getString("triage_status"));
                    row.setLastIntervention(rs.getString("last_intervention"));
                    row.setRecordedAt(rs.getTimestamp("recorded_at"));
                    
                    list.add(row);
                }
            }
        }
        return list;
    }

    // Helper count resolver
    private int fetchCount(Connection conn, String query) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
