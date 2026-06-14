package com.carewatch.features.monitoring.service;

import com.carewatch.features.monitoring.model.PatientVitalView;
import com.carewatch.features.monitoring.repository.MonitoringRepository;
import java.sql.SQLException;
import java.util.List;

// Service containing live monitoring business logic for doctor portal
public class MonitoringService {
    private final MonitoringRepository monitoringRepository = new MonitoringRepository();

    // Fetch the latest vitals signs for patients, matching filters and search keywords
    public List<PatientVitalView> getLiveVitals(String searchKeyword, String urgencyFilter) throws SQLException {
        String queryKeyword = searchKeyword != null ? searchKeyword.trim() : "";
        String queryFilter = urgencyFilter != null ? urgencyFilter.trim() : "ALL";
        return monitoringRepository.findLatestVitals(queryKeyword, queryFilter);
    }
}
