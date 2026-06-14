package com.carewatch.features.intervention.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.core.session.SessionManager;
import com.carewatch.features.intervention.model.DoctorIntervention;
import com.carewatch.features.intervention.service.InterventionService;
import com.carewatch.features.patient.model.Patient;
import com.carewatch.features.user.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Controller for Patient Intervention Log
public class InterventionController {

    private final InterventionService interventionService = new InterventionService();
    private final ObservableList<DoctorIntervention> interventionsList = FXCollections.observableArrayList();

    @FXML
    private ComboBox<Patient> patientComboBox;
    @FXML
    private ComboBox<String> actionComboBox;
    @FXML
    private TextArea notesArea;

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<User> doctorFilterComboBox;

    @FXML
    private TableView<DoctorIntervention> interventionTable;
    @FXML
    private TableColumn<DoctorIntervention, Integer> idColumn;
    @FXML
    private TableColumn<DoctorIntervention, String> patientColumn;
    @FXML
    private TableColumn<DoctorIntervention, String> doctorColumn;
    @FXML
    private TableColumn<DoctorIntervention, String> actionColumn;
    @FXML
    private TableColumn<DoctorIntervention, String> notesColumn;
    @FXML
    private TableColumn<DoctorIntervention, String> dateColumn;

    @FXML
    private StackPane loadingOverlay;

    @FXML
    public void initialize() {
        // Map TableView columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("interventionId"));
        patientColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("doctorName"));
        actionColumn.setCellValueFactory(new PropertyValueFactory<>("actionTaken"));
        notesColumn.setCellValueFactory(new PropertyValueFactory<>("notes"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));

        interventionTable.setItems(interventionsList);

        // Populate standard clinical actions in Combo Box
        actionComboBox.setItems(FXCollections.observableArrayList(
            "Called Patient", 
            "Suggested Medicine", 
            "Emergency Visit Recommended", 
            "Marked as Stable", 
            "Requested Lab Test"
        ));

        // String converters for ComboBox displays
        patientComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Patient p) {
                return p == null ? "" : p.getPatientName() + " (" + p.getPatientEmail() + ")";
            }
            @Override
            public Patient fromString(String string) {
                return null;
            }
        });

        doctorFilterComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(User u) {
                return u == null ? "" : u.getFullName();
            }
            @Override
            public User fromString(String string) {
                return null;
            }
        });

        // Load database metadata and records
        handleRefresh();
    }

    // Refresh directory records and metadata selection options
    private void handleRefresh() {
        loadingOverlay.setVisible(true);
        interventionTable.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                // Fetch selection metadata and logged history
                List<Patient> patients = interventionService.getPatientsList();
                List<User> doctors = interventionService.getDoctorAdminList();
                List<DoctorIntervention> logs = interventionService.getAllInterventions();
                
                // Add dummy User for "All Physicians" filter
                User allPhysicians = new User();
                allPhysicians.setUserId(0);
                allPhysicians.setFullName("All Physicians");
                
                List<User> filterDoctorsList = new ArrayList<>();
                filterDoctorsList.add(allPhysicians);
                filterDoctorsList.addAll(doctors);

                Thread.sleep(200); // Visual transition delay

                Platform.runLater(() -> {
                    patientComboBox.setItems(FXCollections.observableArrayList(patients));
                    doctorFilterComboBox.setItems(FXCollections.observableArrayList(filterDoctorsList));
                    doctorFilterComboBox.setValue(allPhysicians);
                    
                    interventionsList.setAll(logs);
                    
                    loadingOverlay.setVisible(false);
                    interventionTable.setDisable(false);
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    interventionTable.setDisable(false);
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to Load Log History", e.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    interventionTable.setDisable(false);
                    showAlert(Alert.AlertType.ERROR, "System Error", "Failed to Fetch Logs", e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Save doctor intervention log
    @FXML
    private void handleSave() {
        Patient selectedPatient = patientComboBox.getValue();
        String action = actionComboBox.getValue();
        String notes = notesArea.getText();

        if (selectedPatient == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Patient Selection", "Please choose a patient from the list.");
            return;
        }

        if (action == null || action.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Missing Clinical Action", "Please specify the action taken.");
            return;
        }

        User currentDoctor = SessionManager.getCurrentUser();
        if (currentDoctor == null) {
            showAlert(Alert.AlertType.ERROR, "Session Error", "No Active Session", "Physician session has expired. Please sign in again.");
            handleBack();
            return;
        }

        DoctorIntervention log = new DoctorIntervention();
        log.setPatientId(selectedPatient.getPatientId());
        log.setDoctorUserId(currentDoctor.getUserId());
        log.setActionTaken(action.trim());
        log.setNotes(notes != null ? notes.trim() : "");

        loadingOverlay.setVisible(true);

        Thread thread = new Thread(() -> {
            try {
                interventionService.addIntervention(log);
                Thread.sleep(300); // Visual transition delay

                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Intervention Recorded", "The medical action note has been successfully stored.");
                    handleClear();
                    loadFilteredInterventions(true);
                });

            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.WARNING, "Validation Notice", "Unable to Save Log", e.getMessage());
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Save Operation Failed", e.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "System Error", "Action Note could not be recorded", e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Load logs matching keyword and doctor filters
    private void loadFilteredInterventions(boolean showLoader) {
        String keyword = searchField.getText();
        User selectedDoctor = doctorFilterComboBox.getValue();
        int doctorId = selectedDoctor != null ? selectedDoctor.getUserId() : 0;

        if (showLoader) {
            loadingOverlay.setVisible(true);
            interventionTable.setDisable(true);
        }

        Thread thread = new Thread(() -> {
            try {
                List<DoctorIntervention> logs = interventionService.searchAndFilterInterventions(keyword, doctorId);
                Platform.runLater(() -> {
                    interventionsList.setAll(logs);
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                        interventionTable.setDisable(false);
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                        interventionTable.setDisable(false);
                    }
                    System.err.println("Database error loading filtered logs: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Handles keystrokes in search text area
    @FXML
    private void handleSearchKeyPress() {
        loadFilteredInterventions(false);
    }

    // Handles search button click
    @FXML
    private void handleSearch() {
        loadFilteredInterventions(true);
    }

    // Handles doctor dropdown changes
    @FXML
    private void handleFilter() {
        loadFilteredInterventions(true);
    }

    // Handles force refresh button click
    @FXML
    private void handleRefreshManual() {
        loadFilteredInterventions(true);
    }

    // Reset input form controls
    @FXML
    private void handleClear() {
        patientComboBox.setValue(null);
        actionComboBox.setValue(null);
        notesArea.clear();
        interventionTable.getSelectionModel().clearSelection();
    }

    // SPA switch back to Doctor portal dashboard
    @FXML
    private void handleBack() {
        SceneNavigator.navigate("/views/dashboard/doctor_dashboard.fxml", "CareWatch - Doctor Portal");
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
