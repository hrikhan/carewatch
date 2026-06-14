package com.carewatch.features.triage.service;

import com.carewatch.features.triage.model.TriageItem;
import com.carewatch.features.triage.repository.TriageRepository;
import java.sql.SQLException;
import java.util.List;

// Service layer containing business logic for the Triage Queue Feature
public class TriageService {
    private final TriageRepository triageRepository = new TriageRepository();

    // Invoked automatically when a patient submits vitals
    public void createOrUpdateTriageItem(int patientId, int vitalId, String urgencyLevel, String alertMessage) throws SQLException {
        if (urgencyLevel == null || "NORMAL".equalsIgnoreCase(urgencyLevel)) {
            return; // NORMAL readings do not require triage queue actions
        }

        // Map urgency level to priority score: CRITICAL = 1 (highest), WARNING = 2
        int priorityScore = "CRITICAL".equalsIgnoreCase(urgencyLevel) ? 1 : 2;

        TriageItem item = new TriageItem();
        item.setPatientId(patientId);
        item.setVitalId(vitalId);
        item.setUrgencyLevel(urgencyLevel.toUpperCase());
        item.setPriorityScore(priorityScore);
        item.setAlertMessage(alertMessage);

        triageRepository.createOrUpdateTriageItem(item);
    }

    // Retrieve active pending patients in queue
    public List<TriageItem> getPendingQueue() throws SQLException {
        return triageRepository.findPendingQueue();
    }

    // Retrieve all triage queue logs
    public List<TriageItem> getAllQueue() throws SQLException {
        return triageRepository.findAllQueue();
    }

    // Search triage log by keyword
    public List<TriageItem> searchQueue(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllQueue();
        }
        return triageRepository.searchQueue(keyword.trim());
    }

    // Filter triage log by status
    public List<TriageItem> filterQueueByStatus(String status) throws SQLException {
        String filter = status != null ? status.trim() : "ALL";
        return triageRepository.filterQueueByStatus(filter);
    }

    // Combined search and filtering logic
    public List<TriageItem> searchAndFilterQueue(String keyword, String status) throws SQLException {
        String queryKeyword = keyword != null ? keyword.trim() : "";
        String queryStatus = status != null ? status.trim() : "ALL";
        return triageRepository.searchAndFilterQueue(queryKeyword, queryStatus);
    }

    // Resolve an emergency queue item
    public void resolveTriageItem(int queueId) throws SQLException {
        triageRepository.markAsResolved(queueId);
    }
}
