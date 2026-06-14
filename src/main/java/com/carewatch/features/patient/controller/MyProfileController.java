package com.carewatch.features.patient.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.core.session.SessionManager;
import com.carewatch.features.patient.model.Patient;
import com.carewatch.features.patient.service.PatientService;
import com.carewatch.features.user.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Controller for patient personal profile view
public class MyProfileController {

    private final PatientService patientService = new PatientService();

    @FXML
    private Label patientNameLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label ageLabel;
    @FXML
    private Label genderLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label addressLabel;
    @FXML
    private Label diseaseLabel;
    @FXML
    private Label riskLabel;
    @FXML
    private Label doctorLabel;
    @FXML
    private Label emergencyLabel;

    @FXML
    private StackPane loadingOverlay;
    @FXML
    private VBox mainContentBox;

    @FXML
    public void initialize() {
        loadProfileData();
    }

    // Load active patient profile from DB
    private void loadProfileData() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            handleBack();
            return;
        }

        patientNameLabel.setText(currentUser.getFullName());
        emailLabel.setText(currentUser.getEmail());

        loadingOverlay.setVisible(true);
        mainContentBox.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                Patient profile = patientService.getMyProfile(currentUser.getUserId());
                Thread.sleep(300); // Latency simulator for progress animation

                Platform.runLater(() -> {
                    if (profile != null) {
                        ageLabel.setText(profile.getAge() + " Years");
                        genderLabel.setText(profile.getGender());
                        phoneLabel.setText(profile.getPhone());
                        addressLabel.setText(profile.getAddress() != null && !profile.getAddress().trim().isEmpty() ? profile.getAddress() : "N/A");
                        diseaseLabel.setText(profile.getDiseaseType());
                        riskLabel.setText(profile.getRiskLevel());
                        doctorLabel.setText(profile.getAssignedDoctorName());
                        emergencyLabel.setText(profile.getEmergencyContact());
                    } else {
                        ageLabel.setText("Not Configured");
                        genderLabel.setText("Not Configured");
                        phoneLabel.setText("Not Configured");
                        addressLabel.setText("Not Configured");
                        diseaseLabel.setText("Not Configured");
                        riskLabel.setText("Not Configured");
                        doctorLabel.setText("Not Assigned");
                        emergencyLabel.setText("Not Configured");
                    }
                    loadingOverlay.setVisible(false);
                    mainContentBox.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    mainContentBox.setDisable(false);
                    System.err.println("Error loading profile: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Redirect to patient dashboard
    @FXML
    private void handleBack() {
        SceneNavigator.navigate("/views/dashboard/patient_dashboard.fxml", "CareWatch - Patient Portal");
    }
}
