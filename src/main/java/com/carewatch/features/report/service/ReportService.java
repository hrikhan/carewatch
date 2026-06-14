package com.carewatch.features.report.service;

import com.carewatch.features.report.model.ReportRow;
import com.carewatch.features.report.repository.ReportRepository;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Service containing reports generation and file export logic
public class ReportService {
    private final ReportRepository repository = new ReportRepository();

    // Fetch metric counts for summary cards
    public Map<String, Integer> getSummaryCounts() throws SQLException {
        return repository.getSummaryCounts();
    }

    // Fetch all logs
    public List<ReportRow> getReportRows() throws SQLException {
        return repository.findReportRows();
    }

    // Search and filter report logs
    public List<ReportRow> searchReports(String keyword, String urgency, LocalDate startDate, LocalDate endDate) throws SQLException {
        return repository.searchAndFilterReports(keyword, urgency, startDate, endDate);
    }

    // Export report lines list to standard CSV file format
    public void exportToCsv(List<ReportRow> rows, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write CSV headers
            writer.write("Patient Name,Email,Phone,Disease Type,Heart Rate (bpm),Oxygen Level (%),BP (mmHg),Temperature (°F),Sugar Level (mg/dL),Urgency Level,Triage Status,Last Intervention,Recorded At");
            writer.newLine();
            
            for (ReportRow row : rows) {
                writer.write(escapeCsv(row.getPatientName()) + "," +
                             escapeCsv(row.getEmail()) + "," +
                             escapeCsv(row.getPhone()) + "," +
                             escapeCsv(row.getDiseaseType()) + "," +
                             row.getHeartRate() + "," +
                             row.getOxygenLevel() + "," +
                             row.getBp() + "," +
                             row.getTemperature() + "," +
                             row.getSugarLevel() + "," +
                             row.getUrgencyLevel() + "," +
                             escapeCsv(row.getTriageStatus()) + "," +
                             escapeCsv(row.getLastIntervention()) + "," +
                             row.getFormattedDate());
                writer.newLine();
            }
        }
    }

    // Escapes special characters for CSV formatting
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
