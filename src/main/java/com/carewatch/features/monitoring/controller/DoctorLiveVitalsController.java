package com.carewatch.features.monitoring.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.core.session.SessionManager;
import com.carewatch.features.monitoring.model.PatientVitalView;
import com.carewatch.features.monitoring.service.MonitoringService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Controller for doctor live vitals monitoring dashboard
public class DoctorLiveVitalsController {

    private final MonitoringService monitoringService = new MonitoringService();
    private final ObservableList<PatientVitalView> patientVitalsList = FXCollections.observableArrayList();
    private final Set<Integer> notifiedCriticalPatientIds = new HashSet<>();
    
    private Timeline autoRefreshTimeline;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> urgencyFilterComboBox;
    @FXML
    private Label refreshStatusLabel;
    
    @FXML
    private TableView<PatientVitalView> vitalsTable;
    @FXML
    private TableColumn<PatientVitalView, Integer> patientIdColumn;
    @FXML
    private TableColumn<PatientVitalView, String> nameColumn;
    @FXML
    private TableColumn<PatientVitalView, String> emailColumn;
    @FXML
    private TableColumn<PatientVitalView, String> phoneColumn;
    @FXML
    private TableColumn<PatientVitalView, String> diseaseColumn;
    @FXML
    private TableColumn<PatientVitalView, Integer> heartRateColumn;
    @FXML
    private TableColumn<PatientVitalView, Integer> oxygenColumn;
    @FXML
    private TableColumn<PatientVitalView, String> bpColumn;
    @FXML
    private TableColumn<PatientVitalView, Double> tempColumn;
    @FXML
    private TableColumn<PatientVitalView, Integer> sugarColumn;
    @FXML
    private TableColumn<PatientVitalView, String> urgencyColumn;
    @FXML
    private TableColumn<PatientVitalView, String> dateColumn;

    @FXML
    private StackPane loadingOverlay;

    @FXML
    public void initialize() {
        // Map table columns to PatientVitalView properties
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        diseaseColumn.setCellValueFactory(new PropertyValueFactory<>("diseaseType"));
        heartRateColumn.setCellValueFactory(new PropertyValueFactory<>("heartRate"));
        oxygenColumn.setCellValueFactory(new PropertyValueFactory<>("oxygenLevel"));
        bpColumn.setCellValueFactory(new PropertyValueFactory<>("bp"));
        tempColumn.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        sugarColumn.setCellValueFactory(new PropertyValueFactory<>("sugarLevel"));
        urgencyColumn.setCellValueFactory(new PropertyValueFactory<>("urgencyLevel"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));

        // Setup custom row styling based on Patient Vital Urgency
        vitalsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(PatientVitalView item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    String level = item.getUrgencyLevel();
                    if ("CRITICAL".equalsIgnoreCase(level)) {
                        setStyle("-fx-background-color: rgba(248, 81, 73, 0.15);"); // Light Red
                    } else if ("WARNING".equalsIgnoreCase(level)) {
                        setStyle("-fx-background-color: rgba(240, 136, 59, 0.15);"); // Light Orange
                    } else {
                        setStyle("-fx-background-color: rgba(86, 211, 100, 0.08);"); // Light Green
                    }
                }
            }
        });

        // Setup custom text coloring on the urgency level cells
        urgencyColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("CRITICAL".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ff7b72; -fx-font-weight: bold;");
                    } else if ("WARNING".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #f0883b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #56d364; -fx-font-weight: bold;");
                    }
                }
            }
        });

        vitalsTable.setItems(patientVitalsList);

        // Fill combo options
        urgencyFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "NORMAL", "WARNING", "CRITICAL"));
        urgencyFilterComboBox.setValue("ALL");

        // Initial loading state
        loadData(true);

        // Configure auto refresh
        startAutoRefresh();

        // Listen for scene detach to stop background Timeline
        vitalsTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) {
                stopAutoRefresh();
            }
        });
    }

    // Load and update vitals list asynchronously
    private void loadData(boolean showLoader) {
        String keyword = searchField.getText();
        String urgency = urgencyFilterComboBox.getValue();

        if (showLoader) {
            loadingOverlay.setVisible(true);
            vitalsTable.setDisable(true);
        }

        Thread thread = new Thread(() -> {
            try {
                List<PatientVitalView> latestVitals = monitoringService.getLiveVitals(keyword, urgency);
                
                // Track critical alerts
                List<PatientVitalView> newCriticals = new ArrayList<>();
                for (PatientVitalView pv : latestVitals) {
                    if ("CRITICAL".equalsIgnoreCase(pv.getUrgencyLevel())) {
                        if (!notifiedCriticalPatientIds.contains(pv.getPatientId())) {
                            notifiedCriticalPatientIds.add(pv.getPatientId());
                            newCriticals.add(pv);
                        }
                    } else {
                        notifiedCriticalPatientIds.remove(pv.getPatientId());
                    }
                }

                Platform.runLater(() -> {
                    patientVitalsList.setAll(latestVitals);
                    
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                        vitalsTable.setDisable(false);
                    }

                    // Alert doctor about any newly detected critical situations
                    if (!newCriticals.isEmpty()) {
                        StringBuilder sb = new StringBuilder("The following patients have registered CRITICAL vital signs and require immediate attention:\n\n");
                        for (PatientVitalView p : newCriticals) {
                            sb.append("- ").append(p.getPatientName())
                              .append(" (ID: ").append(p.getPatientId()).append(")\n")
                              .append("  HR: ").append(p.getHeartRate()).append(" bpm, ")
                              .append("Oxygen: ").append(p.getOxygenLevel()).append("%, ")
                              .append("Temp: ").append(p.getTemperature()).append("°F\n\n");
                        }
                        showAlert(Alert.AlertType.ERROR, "Critical Vitals Alert", "EMERGENCY: Critical Patients Detected", sb.toString());
                    }
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                        vitalsTable.setDisable(false);
                    }
                    System.err.println("Database error loading monitoring data: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Live search query matching on key presses inside input field
    @FXML
    private void handleSearchKeyPress() {
        loadData(false);
    }

    // Click trigger on Search button
    @FXML
    private void handleSearch() {
        loadData(true);
    }

    // Urgency ComboBox change handler
    @FXML
    private void handleFilter() {
        loadData(true);
    }

    // Manual Refresh button handler
    @FXML
    private void handleRefreshManual() {
        loadData(true);
    }

    // Back to doctor home portal dashboard
    @FXML
    private void handleBack() {
        stopAutoRefresh();
        SceneNavigator.navigate("/views/dashboard/doctor_dashboard.fxml", "CareWatch - Doctor Portal");
    }

    // Start auto-refresh timer loop
    private void startAutoRefresh() {
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            loadData(false);
        }));
        autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
        autoRefreshTimeline.play();
    }

    // Terminate auto-refresh timeline loop
    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            autoRefreshTimeline = null;
        }
    }

    // Display styled blocking dialogs
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
