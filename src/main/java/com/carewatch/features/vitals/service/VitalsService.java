package com.carewatch.features.vitals.service;

import com.carewatch.features.vitals.model.Vital;
import com.carewatch.features.vitals.repository.VitalsRepository;
import com.carewatch.features.triage.service.TriageService;
import java.sql.SQLException;
import java.util.List;

// Service layer for Patient Vitals
public class VitalsService {
    private final VitalsRepository vitalsRepository = new VitalsRepository();
    private final TriageService triageService = new TriageService();

    // Find patient_id associated with a user_id
    public int getPatientIdByUserId(int userId) throws SQLException, IllegalArgumentException {
        int patientId = vitalsRepository.findPatientIdByUserId(userId);
        if (patientId == -1) {
            throw new IllegalArgumentException("No patient profile found. Please contact Super Admin to configure your patient profile first.");
        }
        return patientId;
    }

    // Automatically calculate medical urgency level based on metrics rules:
    // - CRITICAL: oxygen < 90% OR heart_rate > 130 bpm OR heart_rate < 45 bpm OR temperature >= 103°F
    // - WARNING: oxygen < 95% OR heart_rate > 110 bpm OR temperature >= 100.4°F
    // - NORMAL: default
    public String calculateUrgency(int heartRate, int oxygenLevel, double temperature) {
        if (oxygenLevel < 90 || heartRate > 130 || heartRate < 45 || temperature >= 103.0) {
            return "CRITICAL";
        }
        if (oxygenLevel < 95 || heartRate > 110 || temperature >= 100.4) {
            return "WARNING";
        }
        return "NORMAL";
    }

    // Save vitals entry after thorough input validation
    public void submitVitals(Vital vital) throws SQLException, IllegalArgumentException {
        validateVitals(vital);
        
        // Compute the urgency level right before saving
        String calculatedUrgency = calculateUrgency(vital.getHeartRate(), vital.getOxygenLevel(), vital.getTemperature());
        vital.setUrgencyLevel(calculatedUrgency);
        
        vitalsRepository.saveVitals(vital);

        // If urgency level indicates warning or critical state, log to triage queue
        if ("WARNING".equalsIgnoreCase(calculatedUrgency) || "CRITICAL".equalsIgnoreCase(calculatedUrgency)) {
            String alertMessage = "Patient has " + calculatedUrgency + " vitals: " +
                                  "Heart Rate " + vital.getHeartRate() + " bpm, " +
                                  "Oxygen " + vital.getOxygenLevel() + "%, " +
                                  "Temp " + vital.getTemperature() + "°F. " +
                                  (vital.getSymptoms() != null ? vital.getSymptoms() : "");
            triageService.createOrUpdateTriageItem(vital.getPatientId(), vital.getVitalId(), calculatedUrgency, alertMessage);
        }
    }

    // Retrieve full vitals history log for a patient
    public List<Vital> getMyVitalsHistory(int patientId) throws SQLException {
        return vitalsRepository.findVitalsByPatientId(patientId);
    }

    // Retrieve latest vitals record for monitoring/dashboard
    public Vital getLatestVitals(int patientId) throws SQLException {
        return vitalsRepository.findLatestVitalsByPatientId(patientId);
    }

    // Validate vitals metric ranges
    private void validateVitals(Vital v) throws IllegalArgumentException {
        if (v.getPatientId() <= 0) {
            throw new IllegalArgumentException("Invalid patient identifier.");
        }
        if (v.getHeartRate() < 30 || v.getHeartRate() > 250) {
            throw new IllegalArgumentException("Heart rate must be between 30 and 250 bpm.");
        }
        if (v.getOxygenLevel() < 50 || v.getOxygenLevel() > 100) {
            throw new IllegalArgumentException("Oxygen level must be between 50% and 100%.");
        }
        if (v.getSystolicBp() < 50 || v.getSystolicBp() > 250) {
            throw new IllegalArgumentException("Systolic blood pressure must be between 50 and 250 mmHg.");
        }
        if (v.getDiastolicBp() < 30 || v.getDiastolicBp() > 180) {
            throw new IllegalArgumentException("Diastolic blood pressure must be between 30 and 180 mmHg.");
        }
        if (v.getTemperature() < 90.0 || v.getTemperature() > 115.0) {
            throw new IllegalArgumentException("Temperature must be between 90.0°F and 115.0°F.");
        }
        if (v.getSugarLevel() < 10 || v.getSugarLevel() > 600) {
            throw new IllegalArgumentException("Sugar level must be between 10 and 600 mg/dL.");
        }
        if (v.getSymptoms() != null && v.getSymptoms().length() > 500) {
            throw new IllegalArgumentException("Symptoms details cannot exceed 500 characters.");
        }
    }
}
