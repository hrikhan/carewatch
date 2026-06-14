package com.carewatch.features.patient.service;

import com.carewatch.features.patient.model.Patient;
import com.carewatch.features.patient.repository.PatientRepository;
import com.carewatch.features.user.model.User;
import java.sql.SQLException;
import java.util.List;

// Service containing patient profiles business logic
public class PatientService {
    private final PatientRepository patientRepository = new PatientRepository();

    // Load active patient users
    public List<User> loadPatientUsers() throws SQLException {
        return patientRepository.findPatientUsersWithoutOrWithProfile();
    }

    // Load active doctor admin users
    public List<User> loadDoctorUsers() throws SQLException {
        return patientRepository.findDoctorAdminUsers();
    }

    // Create a new patient medical profile
    public void createPatientProfile(Patient patient) throws SQLException, IllegalArgumentException {
        validatePatientProfile(patient, true);
        patientRepository.createPatientProfile(patient);
    }

    // Update patient medical profile details
    public void updatePatientProfile(Patient patient) throws SQLException, IllegalArgumentException {
        validatePatientProfile(patient, false);
        patientRepository.updatePatientProfile(patient);
    }

    // Retrieve all patient profiles
    public List<Patient> getAllPatientProfiles() throws SQLException {
        return patientRepository.findAllPatientProfiles();
    }

    // Search profiles by keyword
    public List<Patient> searchPatientProfiles(String keyword) throws SQLException {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllPatientProfiles();
        }
        return patientRepository.searchPatientProfiles(keyword.trim());
    }

    // Fetch patient profile by current active user id
    public Patient getMyProfile(int currentUserId) throws SQLException {
        return patientRepository.findPatientProfileByUserId(currentUserId);
    }

    // Validate patient inputs
    private void validatePatientProfile(Patient p, boolean isCreate) throws IllegalArgumentException {
        if (isCreate && p.getUserId() <= 0) {
            throw new IllegalArgumentException("Patient user must be selected.");
        }
        if (p.getAge() <= 0 || p.getAge() > 150) {
            throw new IllegalArgumentException("Age must be between 1 and 150.");
        }
        if (p.getGender() == null || p.getGender().trim().isEmpty()) {
            throw new IllegalArgumentException("Gender must be selected.");
        }
        if (p.getPhone() == null || p.getPhone().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        if (p.getDiseaseType() == null || p.getDiseaseType().trim().isEmpty()) {
            throw new IllegalArgumentException("Disease Type cannot be empty.");
        }
        if (p.getRiskLevel() == null || p.getRiskLevel().trim().isEmpty()) {
            throw new IllegalArgumentException("Risk Level must be selected.");
        }
        if (p.getEmergencyContact() == null || p.getEmergencyContact().trim().isEmpty()) {
            throw new IllegalArgumentException("Emergency contact cannot be empty.");
        }
    }
}
