package com.carewatch.features.intervention.service;

import com.carewatch.features.intervention.model.DoctorIntervention;
import com.carewatch.features.intervention.repository.InterventionRepository;
import com.carewatch.features.patient.model.Patient;
import com.carewatch.features.user.model.User;
import java.sql.SQLException;
import java.util.List;

// Service layer for Doctor Interventions
public class InterventionService {
    private final InterventionRepository repository = new InterventionRepository();

    // Save medical intervention note with validations
    public void addIntervention(DoctorIntervention intervention) throws SQLException, IllegalArgumentException {
        if (intervention.getPatientId() <= 0) {
            throw new IllegalArgumentException("A valid patient must be selected.");
        }
        if (intervention.getDoctorUserId() <= 0) {
            throw new IllegalArgumentException("A valid doctor/admin user session is required.");
        }
        if (intervention.getActionTaken() == null || intervention.getActionTaken().trim().isEmpty()) {
            throw new IllegalArgumentException("Action Taken cannot be empty.");
        }
        repository.saveIntervention(intervention);
    }

    // Retrieve all recorded doctor interventions
    public List<DoctorIntervention> getAllInterventions() throws SQLException {
        return repository.findAllInterventions();
    }

    // Retrieve patients dropdown select options list
    public List<Patient> getPatientsList() throws SQLException {
        return repository.findPatientsFromTriageOrAllPatients();
    }

    // Retrieve doctor admin list for filter combo options
    public List<User> getDoctorAdminList() throws SQLException {
        return repository.findDoctorAdminUsers();
    }

    // Search and filter query action
    public List<DoctorIntervention> searchAndFilterInterventions(String keyword, int doctorUserId) throws SQLException {
        return repository.searchAndFilterInterventions(keyword, doctorUserId);
    }
}
