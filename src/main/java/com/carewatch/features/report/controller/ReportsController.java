package com.carewatch.features.report.controller;

import com.carewatch.core.constants.Role;
import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.core.session.SessionManager;
import com.carewatch.features.report.model.ReportRow;
import com.carewatch.features.report.service.ReportService;
import com.carewatch.features.user.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Controller class for Reports & Analytics view
public class ReportsController {

    private final ReportService reportService = new ReportService();
    private final ObservableList<ReportRow> reportRowsList = FXCollections.observableArrayList();

    @FXML
    private Label txtTotalPatients;
    @FXML
    private Label txtTotalVitals;
    @FXML
    private Label txtCriticalCases;
    @FXML
    private Label txtWarningCases;
    @FXML
    private Label txtResolvedCases;
    @FXML
    private Label txtTotalInterventions;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> urgencyFilterComboBox;
    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TableView<ReportRow> reportsTable;
    @FXML
    private TableColumn<ReportRow, String> nameColumn;
    @FXML
    private TableColumn<ReportRow, String> emailColumn;
    @FXML
    private TableColumn<ReportRow, String> phoneColumn;
    @FXML
    private TableColumn<ReportRow, String> diseaseColumn;
    @FXML
    private TableColumn<ReportRow, Integer> heartRateColumn;
    @FXML
    private TableColumn<ReportRow, Integer> oxygenColumn;
    @FXML
    private TableColumn<ReportRow, String> bpColumn;
    @FXML
    private TableColumn<ReportRow, Double> tempColumn;
    @FXML
    private TableColumn<ReportRow, Integer> sugarColumn;
    @FXML
    private TableColumn<ReportRow, String> urgencyColumn;
    @FXML
    private TableColumn<ReportRow, String> triageColumn;
    @FXML
    private TableColumn<ReportRow, String> interventionColumn;
    @FXML
    private TableColumn<ReportRow, String> dateColumn;

    @FXML
    private StackPane loadingOverlay;

    @FXML
    public void initialize() {
        // Map table columns to ReportRow properties
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
        triageColumn.setCellValueFactory(new PropertyValueFactory<>("triageStatus"));
        interventionColumn.setCellValueFactory(new PropertyValueFactory<>("lastIntervention"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));

        // Setup custom row styling based on Urgency
        reportsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(ReportRow item, boolean empty) {
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

        // Triage status column cell factory
        triageColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("RESOLVED".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #56d364; -fx-font-weight: bold;");
                    } else if ("PENDING".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #f0883b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #8b949e;");
                    }
                }
            }
        });

        reportsTable.setItems(reportRowsList);

        // Fill combo options
        urgencyFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "NORMAL", "WARNING", "CRITICAL"));
        urgencyFilterComboBox.setValue("ALL");

        // Initial loading state
        loadData(true);
    }

    // Load metrics and report rows from database
    private void loadData(boolean showLoader) {
        String keyword = searchField.getText();
        String urgency = urgencyFilterComboBox.getValue();
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (showLoader) {
            loadingOverlay.setVisible(true);
            reportsTable.setDisable(true);
        }

        Thread thread = new Thread(() -> {
            try {
                // Fetch metrics first
                Map<String, Integer> counts = reportService.getSummaryCounts();
                // Fetch rows
                List<ReportRow> rows = reportService.searchReports(keyword, urgency, start, end);

                Platform.runLater(() -> {
                    // Update labels
                    txtTotalPatients.setText(String.valueOf(counts.getOrDefault("Total Patients", 0)));
                    txtTotalVitals.setText(String.valueOf(counts.getOrDefault("Total Vitals Submitted", 0)));
                    txtCriticalCases.setText(String.valueOf(counts.getOrDefault("Critical Cases", 0)));
                    txtWarningCases.setText(String.valueOf(counts.getOrDefault("Warning Cases", 0)));
                    txtResolvedCases.setText(String.valueOf(counts.getOrDefault("Resolved Triage Cases", 0)));
                    txtTotalInterventions.setText(String.valueOf(counts.getOrDefault("Total Doctor Interventions", 0)));

                    // Update Table
                    reportRowsList.setAll(rows);

                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                        reportsTable.setDisable(false);
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                        reportsTable.setDisable(false);
                    }
                    System.err.println("Database error loading reports data: " + e.getMessage());
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to Load Reports", e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Filter event handler
    @FXML
    private void handleFilter() {
        loadData(false);
    }

    // KeyRelease handler for searchField
    @FXML
    private void handleSearchKeyPress() {
        loadData(false);
    }

    // Reset filters
    @FXML
    private void handleResetFilters() {
        searchField.clear();
        urgencyFilterComboBox.setValue("ALL");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        loadData(true);
    }

    // Force refresh button
    @FXML
    private void handleRefresh() {
        loadData(true);
    }

    // Export reportsTable items to CSV file
    @FXML
    private void handleExportCsv() {
        if (reportRowsList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Data", "Export Failed", "There is no data currently available in the table to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Telemetry CSV Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files (*.csv)", "*.csv"));
        fileChooser.setInitialFileName("patient_telemetry_report_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(reportsTable.getScene().getWindow());
        if (file != null) {
            loadingOverlay.setVisible(true);
            Thread thread = new Thread(() -> {
                try {
                    reportService.exportToCsv(reportRowsList, file);
                    Platform.runLater(() -> {
                        loadingOverlay.setVisible(false);
                        showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Data Exported", "Report has been exported successfully to " + file.getAbsolutePath());
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> {
                        loadingOverlay.setVisible(false);
                        showAlert(Alert.AlertType.ERROR, "Export Failed", "I/O Error", "Could not export CSV file: " + e.getMessage());
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
    }

    // Navigate back to the caller portal dashboard
    @FXML
    private void handleBack() {
        User user = SessionManager.getCurrentUser();
        if (user != null && user.getRoleEnum() == Role.SUPER_ADMIN) {
            SceneNavigator.navigate("/views/dashboard/super_admin_dashboard.fxml", "CareWatch - System Management");
        } else {
            SceneNavigator.navigate("/views/dashboard/doctor_dashboard.fxml", "CareWatch - Doctor Portal");
        }
    }

    // Alert dialog presentation helper
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
