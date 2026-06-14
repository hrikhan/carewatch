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

// Controller for Doctor Dashboard
public class DoctorDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnLiveVitals;
    @FXML
    private Button btnTriage;
    @FXML
    private Button btnInterventions;
    @FXML
    private Button btnReports;

    private boolean dashboardLoaded = false;

    // FXML fields in doctor_dashboard_content.fxml
    @FXML
    private Label lblActivePatients;
    @FXML
    private Label lblActivePatientsSub;
    @FXML
    private Label lblEmergencyAlerts;
    @FXML
    private Label lblEmergencyAlertsSub;
    @FXML
    private Label lblInterventions;
    @FXML
    private Label lblInterventionsSub;

    @FXML
    private javafx.scene.chart.PieChart riskPieChart;
    @FXML
    private javafx.scene.chart.BarChart<String, Number> vitalsBarChart;

    // Initialize dashboard greeting and load default fragment
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
            // Security fallback redirection
            handleLogout();
        }

        // Check if there is a pending SPA fragment redirection
        String pending = SessionManager.getPendingFragment();
        if (pending != null) {
            SessionManager.setPendingFragment(null); // Clear routing trigger
            if (pending.contains("doctor_live_vitals.fxml")) {
                handleShowLiveVitals();
            } else if (pending.contains("triage_queue.fxml")) {
                handleShowTriageQueue();
            } else if (pending.contains("intervention.fxml")) {
                handleShowInterventions();
            } else if (pending.contains("reports.fxml")) {
                handleShowReports();
            } else {
                handleShowDashboard();
            }
        } else {
            handleShowDashboard();
        }
    }

    // Load Doctor Dashboard content fragment
    @FXML
    private void handleShowDashboard() {
        setActiveLink(btnDashboard);
        loadFragment("/views/dashboard/doctor_dashboard_content.fxml");
        loadDashboardData();
    }

    // Redirect to doctor live vitals monitoring view
    @FXML
    private void handleShowLiveVitals() {
        setActiveLink(btnLiveVitals);
        loadFragment("/views/monitoring/doctor_live_vitals.fxml");
    }

    // Load triage queue fragment
    @FXML
    private void handleShowTriageQueue() {
        setActiveLink(btnTriage);
        loadFragment("/views/triage/triage_queue.fxml");
    }

    // Load doctor interventions fragment
    @FXML
    private void handleShowInterventions() {
        setActiveLink(btnInterventions);
        loadFragment("/views/intervention/intervention.fxml");
    }

    // Load Reports and Analytics fragment
    @FXML
    private void handleShowReports() {
        setActiveLink(btnReports);
        loadFragment("/views/report/reports.fxml");
    }

    // Dynamic FXML fragment loader
    private void loadFragment(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (fxmlPath.contains("doctor_dashboard_content.fxml")) {
                loader.setController(this);
            }
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("Failed to load doctor fragment: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // Load clinical summary telemetry and charts dynamically
    private void loadDashboardData() {
        if (lblActivePatients == null) return; // Safeguard if not on dashboard content fragment

        Thread thread = new Thread(() -> {
            int patientsCount = 0;
            int alertsCount = 0;
            int interventionsCount = 0;
            int normalCount = 0;
            int warningCount = 0;
            int criticalCount = 0;

            try (java.sql.Connection conn = com.carewatch.core.config.DatabaseConfig.getConnection()) {
                // Total patients
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM patients")) {
                    if (rs.next()) patientsCount = rs.getInt(1);
                }
                
                // Emergency Alerts (Pending triage status)
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM triage_queue WHERE status = 'PENDING'")) {
                    if (rs.next()) alertsCount = rs.getInt(1);
                }

                // Doctor Interventions
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM doctor_interventions")) {
                    if (rs.next()) interventionsCount = rs.getInt(1);
                }

                // Urgency Distribution
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT urgency_level, COUNT(*) FROM vitals_history GROUP BY urgency_level")) {
                    while (rs.next()) {
                        String level = rs.getString(1);
                        int count = rs.getInt(2);
                        if ("NORMAL".equalsIgnoreCase(level)) normalCount = count;
                        else if ("WARNING".equalsIgnoreCase(level)) warningCount = count;
                        else if ("CRITICAL".equalsIgnoreCase(level)) criticalCount = count;
                    }
                }

            } catch (java.sql.SQLException e) {
                System.err.println("Error loading doctor dashboard analytics: " + e.getMessage());
            }

            final int fPatients = patientsCount;
            final int fAlerts = alertsCount;
            final int fInterventions = interventionsCount;
            final int fNormal = normalCount;
            final int fWarning = warningCount;
            final int fCritical = criticalCount;

            Platform.runLater(() -> {
                if (lblActivePatients != null) {
                    lblActivePatients.setText(fPatients + " Patients");
                    lblActivePatientsSub.setText("System registered");
                }
                if (lblEmergencyAlerts != null) {
                    lblEmergencyAlerts.setText(fAlerts + " High Risk");
                    if (fAlerts > 0) {
                        lblEmergencyAlerts.setStyle("-fx-text-fill: #ff7b72; -fx-font-weight: bold;");
                        lblEmergencyAlertsSub.setText("Urgent care required");
                    } else {
                        lblEmergencyAlerts.setStyle("-fx-text-fill: #56d364; -fx-font-weight: bold;");
                        lblEmergencyAlertsSub.setText("System monitoring stable");
                    }
                }
                if (lblInterventions != null) {
                    lblInterventions.setText(fInterventions + " Actions");
                    lblInterventionsSub.setText("Doctor actions logged");
                }

                // Populate PieChart
                if (riskPieChart != null) {
                    javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData =
                            javafx.collections.FXCollections.observableArrayList();
                    if (fNormal > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Normal (" + fNormal + ")", fNormal));
                    if (fWarning > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Warning (" + fWarning + ")", fWarning));
                    if (fCritical > 0) pieData.add(new javafx.scene.chart.PieChart.Data("Critical (" + fCritical + ")", fCritical));
                    
                    riskPieChart.setData(pieData);
                }

                // Populate BarChart
                if (vitalsBarChart != null) {
                    javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>("NORMAL", fNormal));
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>("WARNING", fWarning));
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>("CRITICAL", fCritical));
                    
                    vitalsBarChart.getData().clear();
                    vitalsBarChart.getData().add(series);
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Set active style on current selected sidebar menu link
    private void setActiveLink(Button activeButton) {
        btnDashboard.getStyleClass().remove("sidebar-button-active");
        if (btnLiveVitals != null) btnLiveVitals.getStyleClass().remove("sidebar-button-active");
        if (btnTriage != null) btnTriage.getStyleClass().remove("sidebar-button-active");
        if (btnInterventions != null) btnInterventions.getStyleClass().remove("sidebar-button-active");
        if (btnReports != null) btnReports.getStyleClass().remove("sidebar-button-active");
        
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }

    // Reset session and logout
    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        SceneNavigator.navigate("/views/auth/login.fxml", "CareWatch - Secure Sign In");
    }
}
