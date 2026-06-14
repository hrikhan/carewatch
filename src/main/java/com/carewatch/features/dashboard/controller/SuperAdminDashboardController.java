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
import java.io.IOException;

// Controller for Super Admin Dashboard
public class SuperAdminDashboardController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private StackPane contentArea;
    @FXML
    private Button btnDashboard;
    @FXML
    private Button btnUsers;
    @FXML
    private Button btnPatients;
    @FXML
    private Button btnReports;

    private boolean dashboardLoaded = false;

    // FXML fields in super_admin_dashboard_content.fxml
    @FXML
    private Label lblDbStatus;
    @FXML
    private Label lblDbStatusSub;
    @FXML
    private Label lblTotalUsers;
    @FXML
    private Label lblTotalUsersSub;
    @FXML
    private Label lblTotalVitals;
    @FXML
    private Label lblTotalVitalsSub;

    @FXML
    private javafx.scene.chart.PieChart rolePieChart;
    @FXML
    private javafx.scene.chart.BarChart<String, Number> diseaseBarChart;

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
        // Load initial dashboard content
        handleShowDashboard();
    }

    // Reset session and logout
    @FXML
    private void handleLogout() {
        SessionManager.clearSession();
        SceneNavigator.navigate("/views/auth/login.fxml", "CareWatch - Secure Sign In");
    }

    // Load Super Admin Dashboard content fragment
    @FXML
    private void handleShowDashboard() {
        setActiveLink(btnDashboard);
        loadFragment("/views/dashboard/super_admin_dashboard_content.fxml");
        loadDashboardData();
    }

    // Load User Management fragment
    @FXML
    private void handleShowUserManagement() {
        setActiveLink(btnUsers);
        loadFragment("/views/user/user_management.fxml");
    }

    // Load Patient Profiles fragment
    @FXML
    private void handleShowPatientProfiles() {
        setActiveLink(btnPatients);
        loadFragment("/views/patient/patient_profile_management.fxml");
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
            // Let the shell controller handle clicks on the buttons in the fragment, if needed
            if (fxmlPath.contains("super_admin_dashboard_content.fxml")) {
                loader.setController(this);
            }
            Parent root = loader.load();
            contentArea.getChildren().setAll(root);
        } catch (IOException e) {
            System.err.println("Failed to load fragment: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // Load system configuration and user analytics dynamically
    private void loadDashboardData() {
        if (lblDbStatus == null) return; // Safeguard if not on dashboard content fragment

        Thread thread = new Thread(() -> {
            long latency = -1;
            int totalUsers = 0;
            int patientCount = 0;
            int staffCount = 0;
            int totalVitals = 0;

            java.util.Map<String, Integer> roleCounts = new java.util.HashMap<>();
            java.util.Map<String, Integer> diseaseCounts = new java.util.HashMap<>();

            try {
                long start = System.currentTimeMillis();
                try (java.sql.Connection conn = com.carewatch.core.config.DatabaseConfig.getConnection()) {
                    try (java.sql.Statement stmt = conn.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT 1")) {
                        if (rs.next()) {
                            latency = System.currentTimeMillis() - start;
                        }
                    }

                    // Total Users by Role
                    try (java.sql.Statement stmt = conn.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT role, COUNT(*) FROM users GROUP BY role")) {
                        while (rs.next()) {
                            String role = rs.getString(1);
                            int cnt = rs.getInt(2);
                            roleCounts.put(role, cnt);
                            totalUsers += cnt;
                            if ("PATIENT".equalsIgnoreCase(role)) {
                                patientCount += cnt;
                            } else {
                                staffCount += cnt;
                            }
                        }
                    }

                    // Total Vitals Submitted
                    try (java.sql.Statement stmt = conn.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM vitals_history")) {
                        if (rs.next()) {
                            totalVitals = rs.getInt(1);
                        }
                    }

                    // Diagnosis categories count
                    try (java.sql.Statement stmt = conn.createStatement();
                         java.sql.ResultSet rs = stmt.executeQuery("SELECT disease_type, COUNT(*) FROM patients GROUP BY disease_type")) {
                        while (rs.next()) {
                            String disease = rs.getString(1);
                            int cnt = rs.getInt(2);
                            if (disease == null || disease.trim().isEmpty()) {
                                disease = "Unspecified";
                            }
                            diseaseCounts.put(disease, cnt);
                        }
                    }
                }
            } catch (java.sql.SQLException e) {
                System.err.println("Database error checking system stats: " + e.getMessage());
            }

            final long fLatency = latency;
            final int fTotalUsers = totalUsers;
            final int fPatients = patientCount;
            final int fStaff = staffCount;
            final int fVitals = totalVitals;
            final java.util.Map<String, Integer> fRoleCounts = roleCounts;
            final java.util.Map<String, Integer> fDiseaseCounts = diseaseCounts;

            javafx.application.Platform.runLater(() -> {
                if (lblDbStatus != null) {
                    if (fLatency >= 0) {
                        lblDbStatus.setText("Online");
                        lblDbStatus.setStyle("-fx-text-fill: #56d364; -fx-font-weight: bold;");
                        lblDbStatusSub.setText("Response time: " + fLatency + "ms");
                    } else {
                        lblDbStatus.setText("Offline");
                        lblDbStatus.setStyle("-fx-text-fill: #ff7b72; -fx-font-weight: bold;");
                        lblDbStatusSub.setText("No connection established");
                    }
                }
                if (lblTotalUsers != null) {
                    lblTotalUsers.setText(fTotalUsers + " Accounts");
                    lblTotalUsersSub.setText(fPatients + " Patients, " + fStaff + " Staff");
                }
                if (lblTotalVitals != null) {
                    lblTotalVitals.setText(fVitals + " Records");
                    lblTotalVitalsSub.setText("Telemetry packets parsed");
                }

                // Populate user roles PieChart
                if (rolePieChart != null) {
                    javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData =
                            javafx.collections.FXCollections.observableArrayList();
                    for (java.util.Map.Entry<String, Integer> entry : fRoleCounts.entrySet()) {
                        pieData.add(new javafx.scene.chart.PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue()));
                    }
                    rolePieChart.setData(pieData);
                }

                // Populate disease distribution BarChart
                if (diseaseBarChart != null) {
                    javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
                    for (java.util.Map.Entry<String, Integer> entry : fDiseaseCounts.entrySet()) {
                        series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }
                    diseaseBarChart.getData().clear();
                    diseaseBarChart.getData().add(series);
                }
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Set active style on current selected sidebar menu link
    private void setActiveLink(Button activeButton) {
        btnDashboard.getStyleClass().remove("sidebar-button-active");
        btnUsers.getStyleClass().remove("sidebar-button-active");
        btnPatients.getStyleClass().remove("sidebar-button-active");
        if (btnReports != null) btnReports.getStyleClass().remove("sidebar-button-active");
        
        if (activeButton != null) {
            activeButton.getStyleClass().add("sidebar-button-active");
        }
    }
}
