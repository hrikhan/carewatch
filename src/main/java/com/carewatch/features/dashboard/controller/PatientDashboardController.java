package com.carewatch.features.dashboard.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.core.session.SessionManager;
import com.carewatch.features.user.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.application.Platform;
import java.io.IOException;

// Controller for Patient Dashboard
public class PatientDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnMyProfile;
    @FXML
    private Button btnSubmitVitals;
    @FXML
    private Button btnVitalsHistory;

    private boolean dashboardLoaded = false;

    // Initialize dashboard greeting and load default dashboard view
    @FXML
    public void initialize() {
        if (dashboardLoaded) {
            return;
        }
        dashboardLoaded = true;

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        } else {
            handleLogout();
        }

        // Check if there is a pending SPA fragment redirection
        String pending = SessionManager.getPendingFragment();
        if (pending != null) {
            SessionManager.setPendingFragment(null); // Clear routing trigger
            if (pending.contains("submit_vitals.fxml")) {
                handleShowSubmitVitals();
            } else if (pending.contains("vitals_history.fxml")) {
                handleShowVitalsHistory();
            } else {
                handleShowDashboard();
            }
        } else {
            // Load initial dashboard content
            handleShowDashboard();
        }
    }

    // Reset session and logout
    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        SceneNavigator.navigate("/views/auth/login.fxml", "CareWatch - Secure Sign In");
    }

    // Load Patient Dashboard content fragment
    @FXML
    private void handleShowDashboard() {
        setActiveLink(btnDashboard);
        loadFragment("/views/dashboard/patient_dashboard_content.fxml");
        loadDashboardData();
    }

    // Load My Profile fragment
    @FXML
    private void handleShowMyProfile() {
        setActiveLink(btnMyProfile);
        loadFragment("/views/patient/my_profile.fxml");
    }

    // Load Submit Vitals fragment
    @FXML
    private void handleShowSubmitVitals() {
        setActiveLink(btnSubmitVitals);
        loadFragment("/views/vitals/submit_vitals.fxml");
    }

    // Load Vitals History fragment
    @FXML
    private void handleShowVitalsHistory() {
        setActiveLink(btnVitalsHistory);
        loadFragment("/views/vitals/vitals_history.fxml");
    }

    // FXML fields in patient_dashboard_content.fxml
    @FXML
    private Label lblHeartRate;
    @FXML
    private Label lblHeartRateSub;
    @FXML
    private Label lblOxygen;
    @FXML
    private Label lblOxygenSub;
    @FXML
    private Label lblBloodPressure;
    @FXML
    private Label lblBloodPressureSub;

    @FXML
    private javafx.scene.chart.LineChart<String, Number> telemetryLineChart;

    // Load patient's vital telemetry metrics and trends dynamically from DB
    private void loadDashboardData() {
        if (lblHeartRate == null) return; // Safeguard if not on dashboard content fragment

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;

        Thread thread = new Thread(() -> {
            Integer patientId = null;
            Integer hr = null;
            Integer o2 = null;
            Integer sysBp = null;
            Integer diaBp = null;
            String urgency = null;
            java.sql.Timestamp recordedAt = null;

            java.util.List<Integer> hrHistory = new java.util.ArrayList<>();
            java.util.List<Integer> o2History = new java.util.ArrayList<>();
            java.util.List<String> datesHistory = new java.util.ArrayList<>();

            try (java.sql.Connection conn = com.carewatch.core.config.DatabaseConfig.getConnection()) {
                // Find patient_id
                try (java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT patient_id FROM patients WHERE user_id = ?")) {
                    stmt.setInt(1, currentUser.getUserId());
                    try (java.sql.ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            patientId = rs.getInt(1);
                        }
                    }
                }

                if (patientId != null) {
                    // Fetch latest vital record
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                            "SELECT heart_rate, oxygen_level, systolic_bp, diastolic_bp, urgency_level, recorded_at " +
                            "FROM vitals_history WHERE patient_id = ? ORDER BY recorded_at DESC LIMIT 1")) {
                        stmt.setInt(1, patientId);
                        try (java.sql.ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                hr = rs.getInt("heart_rate");
                                o2 = rs.getInt("oxygen_level");
                                sysBp = rs.getInt("systolic_bp");
                                diaBp = rs.getInt("diastolic_bp");
                                urgency = rs.getString("urgency_level");
                                recordedAt = rs.getTimestamp("recorded_at");
                            }
                        }
                    }

                    // Fetch latest 10 records for chart
                    try (java.sql.PreparedStatement stmt = conn.prepareStatement(
                            "SELECT heart_rate, oxygen_level, recorded_at " +
                            "FROM vitals_history WHERE patient_id = ? ORDER BY recorded_at DESC LIMIT 10")) {
                        stmt.setInt(1, patientId);
                        try (java.sql.ResultSet rs = stmt.executeQuery()) {
                            while (rs.next()) {
                                hrHistory.add(rs.getInt("heart_rate"));
                                o2History.add(rs.getInt("oxygen_level"));
                                
                                java.sql.Timestamp ts = rs.getTimestamp("recorded_at");
                                String dateStr = "";
                                if (ts != null) {
                                    dateStr = new java.text.SimpleDateFormat("MM-dd HH:mm").format(ts);
                                }
                                datesHistory.add(dateStr);
                            }
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Error loading patient telemetry: " + e.getMessage());
            }

            final Integer fHr = hr;
            final Integer fO2 = o2;
            final Integer fSysBp = sysBp;
            final Integer fDiaBp = diaBp;
            final String fUrgency = urgency;
            final java.sql.Timestamp fRecorded = recordedAt;

            final java.util.List<Integer> fHrHistory = hrHistory;
            final java.util.List<Integer> fO2History = o2History;
            final java.util.List<String> fDatesHistory = datesHistory;

            Platform.runLater(() -> {
                if (lblHeartRate == null) return; // Check if still on screen

                if (fHr != null) {
                    lblHeartRate.setText(fHr + " BPM");
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    lblHeartRateSub.setText("Recorded: " + sdf.format(fRecorded));
                    
                    lblOxygen.setText(fO2 + "%");
                    lblOxygenSub.setText("Oxygen saturation");
                    
                    lblBloodPressure.setText(fSysBp + "/" + fDiaBp + " mmHg");
                    lblBloodPressureSub.setText("Systolic / Diastolic");

                    // Card color coding based on urgency
                    if ("CRITICAL".equalsIgnoreCase(fUrgency)) {
                        lblHeartRate.setStyle("-fx-text-fill: #ff7b72; -fx-font-weight: bold;");
                        lblOxygen.setStyle("-fx-text-fill: #ff7b72; -fx-font-weight: bold;");
                    } else if ("WARNING".equalsIgnoreCase(fUrgency)) {
                        lblHeartRate.setStyle("-fx-text-fill: #f0883b; -fx-font-weight: bold;");
                        lblOxygen.setStyle("-fx-text-fill: #f0883b; -fx-font-weight: bold;");
                    } else {
                        lblHeartRate.setStyle("-fx-text-fill: #56d364; -fx-font-weight: bold;");
                        lblOxygen.setStyle("-fx-text-fill: #56d364; -fx-font-weight: bold;");
                    }
                } else {
                    lblHeartRate.setText("No Data");
                    lblHeartRateSub.setText("Submit vitals to update");
                    lblOxygen.setText("No Data");
                    lblOxygenSub.setText("Submit vitals to update");
                    lblBloodPressure.setText("No Data");
                    lblBloodPressureSub.setText("Submit vitals to update");
                }

                // Populate LineChart
                if (telemetryLineChart != null) {
                    telemetryLineChart.getData().clear();
                    
                    javafx.scene.chart.XYChart.Series<String, Number> hrSeries = new javafx.scene.chart.XYChart.Series<>();
                    hrSeries.setName("Heart Rate (BPM)");
                    
                    javafx.scene.chart.XYChart.Series<String, Number> o2Series = new javafx.scene.chart.XYChart.Series<>();
                    o2Series.setName("Oxygen saturation (%)");

                    // Add chronologically (reverse the DESC lists)
                    for (int i = fHrHistory.size() - 1; i >= 0; i--) {
                        String dateLabel = fDatesHistory.get(i);
                        hrSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(dateLabel, fHrHistory.get(i)));
                        o2Series.getData().add(new javafx.scene.chart.XYChart.Data<>(dateLabel, fO2History.get(i)));
                    }

                    telemetryLineChart.getData().add(hrSeries);
                    telemetryLineChart.getData().add(o2Series);
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Dynamic FXML fragment loader
    private void loadFragment(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (fxmlPath.contains("patient_dashboard_content.fxml")) {
                loader.setController(this);
            }
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("Failed to load fragment: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // Set active style on current selected sidebar menu link
    private void setActiveLink(Button activeButton) {
        btnDashboard.getStyleClass().remove("sidebar-button-active");
        btnMyProfile.getStyleClass().remove("sidebar-button-active");
        if (btnSubmitVitals != null) btnSubmitVitals.getStyleClass().remove("sidebar-button-active");
        if (btnVitalsHistory != null) btnVitalsHistory.getStyleClass().remove("sidebar-button-active");
        
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }
}
