package com.carewatch.features.vitals.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.core.session.SessionManager;
import com.carewatch.features.user.model.User;
import com.carewatch.features.vitals.model.Vital;
import com.carewatch.features.vitals.service.VitalsService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

// Controller for submitting health vitals
public class SubmitVitalsController {

    private final VitalsService vitalsService = new VitalsService();

    @FXML
    private TextField heartRateField;
    @FXML
    private TextField oxygenField;
    @FXML
    private TextField systolicBpField;
    @FXML
    private TextField diastolicBpField;
    @FXML
    private TextField temperatureField;
    @FXML
    private TextField sugarField;
    @FXML
    private TextArea symptomsField;

    @FXML
    private Label urgencyPreviewLabel;
    @FXML
    private Label previewMessageLabel;

    @FXML
    private StackPane loadingOverlay;
    @FXML
    private VBox formContainer;

    @FXML
    public void initialize() {
        resetPreview();
    }

    // Triggered automatically on key releases in vitals metric inputs to calculate live urgency preview
    @FXML
    private void handleInputsChanged() {
        String hrStr = heartRateField.getText().trim();
        String oxStr = oxygenField.getText().trim();
        String tempStr = temperatureField.getText().trim();

        if (hrStr.isEmpty() || oxStr.isEmpty() || tempStr.isEmpty()) {
            resetPreview();
            return;
        }

        try {
            int heartRate = Integer.parseInt(hrStr);
            int oxygen = Integer.parseInt(oxStr);
            double temp = Double.parseDouble(tempStr);

            String urgency = vitalsService.calculateUrgency(heartRate, oxygen, temp);
            urgencyPreviewLabel.setText(urgency);

            if ("CRITICAL".equalsIgnoreCase(urgency)) {
                urgencyPreviewLabel.setStyle("-fx-text-fill: #ff7b72; -fx-font-weight: bold;");
                previewMessageLabel.setText("CRITICAL: Seek immediate medical attention. Your physician will be notified immediately.");
            } else if ("WARNING".equalsIgnoreCase(urgency)) {
                urgencyPreviewLabel.setStyle("-fx-text-fill: #f0883b; -fx-font-weight: bold;");
                previewMessageLabel.setText("WARNING: Vitals are outside standard range. Monitor yourself closely or schedule an appointment.");
            } else {
                urgencyPreviewLabel.setStyle("-fx-text-fill: #56d364; -fx-font-weight: bold;");
                previewMessageLabel.setText("NORMAL: Vitals are within standard clinical ranges.");
            }
        } catch (NumberFormatException e) {
            urgencyPreviewLabel.setText("INVALID INPUTS");
            urgencyPreviewLabel.setStyle("-fx-text-fill: #ff7b72; -fx-font-weight: bold;");
            previewMessageLabel.setText("Please enter valid numeric values for Heart Rate, Oxygen, and Temperature.");
        }
    }

    // Reset urgency status preview to default state
    private void resetPreview() {
        urgencyPreviewLabel.setText("WAITING FOR INPUTS");
        urgencyPreviewLabel.setStyle("-fx-text-fill: #8b949e; -fx-font-weight: bold;");
        previewMessageLabel.setText("Please fill out Heart Rate, Oxygen, and Temperature to view live urgency classification.");
    }

    // Clear form inputs
    @FXML
    private void handleClearForm() {
        heartRateField.clear();
        oxygenField.clear();
        systolicBpField.clear();
        diastolicBpField.clear();
        temperatureField.clear();
        sugarField.clear();
        symptomsField.clear();
        resetPreview();
    }

    // Return to the default patient dashboard content
    @FXML
    private void handleBack() {
        SceneNavigator.navigate("/views/dashboard/patient_dashboard.fxml", "CareWatch - Patient Portal");
    }

    // Handle vitals submission
    @FXML
    private void handleSubmit() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Authentication Error", "No Active Session", "You must be logged in to submit vitals.");
            handleBack();
            return;
        }

        // Validate required fields are not empty
        if (heartRateField.getText().trim().isEmpty() ||
            oxygenField.getText().trim().isEmpty() ||
            systolicBpField.getText().trim().isEmpty() ||
            diastolicBpField.getText().trim().isEmpty() ||
            temperatureField.getText().trim().isEmpty() ||
            sugarField.getText().trim().isEmpty()) {
            
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Fields", "Please fill in all required (*) vitals metrics fields.");
            return;
        }

        // Read and parse vital inputs
        int heartRate, oxygen, systolicBp, diastolicBp, sugar;
        double temperature;
        try {
            heartRate = Integer.parseInt(heartRateField.getText().trim());
            oxygen = Integer.parseInt(oxygenField.getText().trim());
            systolicBp = Integer.parseInt(systolicBpField.getText().trim());
            diastolicBp = Integer.parseInt(diastolicBpField.getText().trim());
            temperature = Double.parseDouble(temperatureField.getText().trim());
            sugar = Integer.parseInt(sugarField.getText().trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Format", "Please enter valid numbers for vital readings.");
            return;
        }

        String symptoms = symptomsField.getText();

        loadingOverlay.setVisible(true);
        formContainer.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                // Fetch the patient profile matching this user
                int patientId = vitalsService.getPatientIdByUserId(currentUser.getUserId());
                
                Vital vital = new Vital();
                vital.setPatientId(patientId);
                vital.setHeartRate(heartRate);
                vital.setOxygenLevel(oxygen);
                vital.setSystolicBp(systolicBp);
                vital.setDiastolicBp(diastolicBp);
                vital.setTemperature(temperature);
                vital.setSugarLevel(sugar);
                vital.setSymptoms(symptoms);

                // Service calculates urgency and saves to DB
                vitalsService.submitVitals(vital);
                Thread.sleep(400); // Progress presentation pause

                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    formContainer.setDisable(false);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Vitals Logged", "Your vital readings have been successfully submitted and analyzed.");
                    
                    // Route to Vitals History view inside SPA
                    SessionManager.setPendingFragment("/views/vitals/vitals_history.fxml");
                    SceneNavigator.navigate("/views/dashboard/patient_dashboard.fxml", "CareWatch - Patient Portal");
                });

            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    formContainer.setDisable(false);
                    showAlert(Alert.AlertType.WARNING, "Validation Notice", "Unable to Submit Vitals", e.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    formContainer.setDisable(false);
                    showAlert(Alert.AlertType.ERROR, "System Error", "Database Operation Failed", e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Helper to generate styled alert boxes
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        DialogPane dialogPane = alert.getDialogPane();
        if (dialogPane != null) {
            try {
                dialogPane.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
                dialogPane.getStyleClass().add("root");
            } catch (Exception e) {
                System.err.println("Warning: CSS could not be loaded for dialog.");
            }
        }
        alert.showAndWait();
    }
}
