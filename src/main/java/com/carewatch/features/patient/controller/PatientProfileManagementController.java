package com.carewatch.features.patient.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.features.patient.model.Patient;
import com.carewatch.features.patient.service.PatientService;
import com.carewatch.features.user.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

// Controller for patient profile management by SUPER_ADMIN
public class PatientProfileManagementController {

    private final PatientService patientService = new PatientService();
    private final ObservableList<Patient> allPatients  = FXCollections.observableArrayList();
    private final ObservableList<Patient> displayList  = FXCollections.observableArrayList();

    @FXML private ComboBox<User>   patientUserComboBox;
    @FXML private TextField        ageField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField        phoneField;
    @FXML private TextArea         addressArea;
    @FXML private TextField        diseaseTypeField;
    @FXML private ComboBox<String> riskLevelComboBox;
    @FXML private ComboBox<User>   assignedDoctorComboBox;
    @FXML private TextField        emergencyContactField;

    // Search and filter
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> riskFilterComboBox;
    @FXML private Label            recordCountLabel;

    // Layout & overlay
    @FXML private StackPane loadingOverlay;
    @FXML private HBox      mainContentBox;

    // Table view
    @FXML private TableView<Patient>             patientTable;
    @FXML private TableColumn<Patient, Integer>  patientIdColumn;
    @FXML private TableColumn<Patient, String>   nameColumn;
    @FXML private TableColumn<Patient, String>   emailColumn;
    @FXML private TableColumn<Patient, Integer>  ageColumn;
    @FXML private TableColumn<Patient, String>   genderColumn;
    @FXML private TableColumn<Patient, String>   phoneColumn;
    @FXML private TableColumn<Patient, String>   diseaseColumn;
    @FXML private TableColumn<Patient, String>   riskColumn;
    @FXML private TableColumn<Patient, String>   doctorColumn;
    @FXML private TableColumn<Patient, String>   emergencyColumn;

    @FXML
    public void initialize() {
        // Dropdown option seeds
        genderComboBox.setItems(FXCollections.observableArrayList("Male", "Female", "Other"));
        riskLevelComboBox.setItems(FXCollections.observableArrayList("LOW", "MEDIUM", "HIGH"));
        riskFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "LOW", "MEDIUM", "HIGH"));
        riskFilterComboBox.setValue("ALL");

        // String converters for User objects in ComboBoxes
        patientUserComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(User u)          { return u == null ? "" : u.getFullName() + " (" + u.getEmail() + ")"; }
            @Override public User fromString(String string)   { return null; }
        });
        assignedDoctorComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(User u)          { return u == null ? "" : u.getFullName(); }
            @Override public User fromString(String string)   { return null; }
        });

        // Column value mappings
        patientIdColumn.setCellValueFactory(new PropertyValueFactory<>("patientId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("patientEmail"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        genderColumn.setCellValueFactory(new PropertyValueFactory<>("gender"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        diseaseColumn.setCellValueFactory(new PropertyValueFactory<>("diseaseType"));
        riskColumn.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        doctorColumn.setCellValueFactory(new PropertyValueFactory<>("assignedDoctorName"));
        emergencyColumn.setCellValueFactory(new PropertyValueFactory<>("emergencyContact"));

        // Row factory — color-code by risk level
        patientTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                setStyle("");
                if (!empty && item != null) {
                    String risk = item.getRiskLevel();
                    if ("HIGH".equalsIgnoreCase(risk)) {
                        setStyle("-fx-background-color: rgba(248, 81, 73, 0.10);");
                    } else if ("MEDIUM".equalsIgnoreCase(risk)) {
                        setStyle("-fx-background-color: rgba(240, 136, 59, 0.10);");
                    }
                }
            }
        });

        // Risk column — badge-style colored cell
        riskColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                if (!empty && item != null) {
                    Label badge = new Label(item);
                    badge.setStyle(getRiskBadgeStyle(item));
                    setGraphic(badge);
                }
            }
            private String getRiskBadgeStyle(String risk) {
                return switch (risk.toUpperCase()) {
                    case "HIGH"   -> "-fx-text-fill: #ff7b72; -fx-font-weight: bold; -fx-background-color: rgba(248,81,73,0.18); -fx-padding: 3px 10px; -fx-background-radius: 4px; -fx-border-color: rgba(248,81,73,0.4); -fx-border-width: 1px; -fx-border-radius: 4px;";
                    case "MEDIUM" -> "-fx-text-fill: #f0883b; -fx-font-weight: bold; -fx-background-color: rgba(240,136,59,0.18); -fx-padding: 3px 10px; -fx-background-radius: 4px; -fx-border-color: rgba(240,136,59,0.4); -fx-border-width: 1px; -fx-border-radius: 4px;";
                    default       -> "-fx-text-fill: #56d364; -fx-font-weight: bold; -fx-background-color: rgba(86,211,100,0.18); -fx-padding: 3px 10px; -fx-background-radius: 4px; -fx-border-color: rgba(86,211,100,0.4); -fx-border-width: 1px; -fx-border-radius: 4px;";
                };
            }
        });

        // Disease column — teal accent
        diseaseColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                setStyle(empty || item == null ? "" : "-fx-text-fill: #58a6ff;");
            }
        });

        patientTable.setItems(displayList);

        // Table row selection -> populate form
        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) populateForm(newVal);
        });

        // Initial data load
        handleRefresh();
    }

    // Populate left form from selected row
    private void populateForm(Patient p) {
        for (User u : patientUserComboBox.getItems()) {
            if (u.getUserId() == p.getUserId()) { patientUserComboBox.setValue(u); break; }
        }
        ageField.setText(String.valueOf(p.getAge()));
        genderComboBox.setValue(p.getGender());
        phoneField.setText(p.getPhone());
        addressArea.setText(p.getAddress());
        diseaseTypeField.setText(p.getDiseaseType());
        riskLevelComboBox.setValue(p.getRiskLevel());
        assignedDoctorComboBox.setValue(null);
        for (User doc : assignedDoctorComboBox.getItems()) {
            if (doc.getUserId() == p.getAssignedDoctorId()) { assignedDoctorComboBox.setValue(doc); break; }
        }
        emergencyContactField.setText(p.getEmergencyContact());
    }

    // Apply in-memory search + risk filter
    private void applyFilters() {
        String keyword = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String risk    = riskFilterComboBox != null ? riskFilterComboBox.getValue() : "ALL";

        List<Patient> result = allPatients.stream()
            .filter(p -> {
                boolean matchesKw = keyword.isEmpty()
                    || (p.getPatientName()  != null && p.getPatientName().toLowerCase().contains(keyword))
                    || (p.getPatientEmail() != null && p.getPatientEmail().toLowerCase().contains(keyword))
                    || (p.getPhone()        != null && p.getPhone().contains(keyword))
                    || (p.getDiseaseType()  != null && p.getDiseaseType().toLowerCase().contains(keyword));
                boolean matchesRisk = "ALL".equalsIgnoreCase(risk)
                    || (p.getRiskLevel() != null && p.getRiskLevel().equalsIgnoreCase(risk));
                return matchesKw && matchesRisk;
            })
            .collect(Collectors.toList());

        displayList.setAll(result);
        updateCountLabel(result.size(), allPatients.size());
    }

    private void updateCountLabel(int shown, int total) {
        if (recordCountLabel == null) return;
        recordCountLabel.setText(shown == total
            ? total + " patient" + (total != 1 ? "s" : "")
            : shown + " of " + total + " shown");
    }

    // Search keypress — in-memory filter (no DB call)
    @FXML
    private void handleSearchKeyPress() { applyFilters(); }

    // Search button click
    @FXML
    private void handleSearch() { applyFilters(); }

    // Risk filter combo change
    @FXML
    private void handleRiskFilter() { applyFilters(); }

    // Full refresh from database
    @FXML
    private void handleRefresh() {
        if (searchField != null) searchField.clear();
        if (riskFilterComboBox != null) riskFilterComboBox.setValue("ALL");
        loadingOverlay.setVisible(true);
        mainContentBox.setDisable(true);
        if (recordCountLabel != null) recordCountLabel.setText("Loading...");

        Thread thread = new Thread(() -> {
            try {
                List<Patient> profiles = patientService.getAllPatientProfiles();
                List<User>    patients = patientService.loadPatientUsers();
                List<User>    doctors  = patientService.loadDoctorUsers();

                Platform.runLater(() -> {
                    allPatients.setAll(profiles);
                    displayList.setAll(profiles);
                    patientUserComboBox.setItems(FXCollections.observableArrayList(patients));
                    assignedDoctorComboBox.setItems(FXCollections.observableArrayList(doctors));
                    updateCountLabel(profiles.size(), profiles.size());
                    loadingOverlay.setVisible(false);
                    mainContentBox.setDisable(false);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    mainContentBox.setDisable(false);
                    if (recordCountLabel != null) recordCountLabel.setText("Error");
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to Load Data", e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Save new profile
    @FXML
    private void handleSaveProfile() {
        User selectedUser = patientUserComboBox.getValue();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "No Patient Selected", "Please select a patient user to create a profile.");
            return;
        }
        int age = parseAge(); if (age == -1) return;

        User selectedDoctor = assignedDoctorComboBox.getValue();
        Patient p = new Patient();
        p.setUserId(selectedUser.getUserId());
        p.setAge(age);
        p.setGender(genderComboBox.getValue());
        p.setPhone(phoneField.getText());
        p.setAddress(addressArea.getText());
        p.setDiseaseType(diseaseTypeField.getText());
        p.setRiskLevel(riskLevelComboBox.getValue());
        p.setAssignedDoctorId(selectedDoctor != null ? selectedDoctor.getUserId() : 0);
        p.setEmergencyContact(emergencyContactField.getText());

        runAsyncTask(
            () -> { try { patientService.createPatientProfile(p); } catch (Exception e) { throw new RuntimeException(e); } },
            () -> { showAlert(Alert.AlertType.INFORMATION, "Success", "Profile Created", "Patient profile successfully created."); handleClear(); handleRefresh(); }
        );
    }

    // Update existing profile
    @FXML
    private void handleUpdateProfile() {
        Patient selected = patientTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "No Profile Selected", "Please select a patient profile from the table to update.");
            return;
        }
        int age = parseAge(); if (age == -1) return;

        User selectedDoctor = assignedDoctorComboBox.getValue();
        selected.setAge(age);
        selected.setGender(genderComboBox.getValue());
        selected.setPhone(phoneField.getText());
        selected.setAddress(addressArea.getText());
        selected.setDiseaseType(diseaseTypeField.getText());
        selected.setRiskLevel(riskLevelComboBox.getValue());
        selected.setAssignedDoctorId(selectedDoctor != null ? selectedDoctor.getUserId() : 0);
        selected.setEmergencyContact(emergencyContactField.getText());

        runAsyncTask(
            () -> { try { patientService.updatePatientProfile(selected); } catch (Exception e) { throw new RuntimeException(e); } },
            () -> { showAlert(Alert.AlertType.INFORMATION, "Success", "Profile Updated", "Patient profile successfully updated."); handleClear(); handleRefresh(); }
        );
    }

    // Clear form inputs
    @FXML
    private void handleClear() {
        patientUserComboBox.setValue(null);
        ageField.clear();
        genderComboBox.setValue(null);
        phoneField.clear();
        addressArea.clear();
        diseaseTypeField.clear();
        riskLevelComboBox.setValue(null);
        assignedDoctorComboBox.setValue(null);
        emergencyContactField.clear();
        patientTable.getSelectionModel().clearSelection();
    }

    // Back action
    @FXML
    private void handleBack() {
        SceneNavigator.navigate("/views/dashboard/super_admin_dashboard.fxml", "CareWatch - System Management");
    }

    // Age validation helper
    private int parseAge() {
        try {
            int age = Integer.parseInt(ageField.getText().trim());
            if (age <= 0 || age > 150) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Age", "Age must be between 1 and 150.");
                return -1;
            }
            return age;
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Age Format", "Age must be a valid integer.");
            return -1;
        }
    }

    // Generic async task runner with loader overlay
    private void runAsyncTask(Runnable dbOp, Runnable uiUpdate) {
        loadingOverlay.setVisible(true);
        mainContentBox.setDisable(true);
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(300);
                dbOp.run();
                Platform.runLater(() -> {
                    uiUpdate.run();
                    loadingOverlay.setVisible(false);
                    mainContentBox.setDisable(false);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    mainContentBox.setDisable(false);
                    showAlert(Alert.AlertType.ERROR, "Operation Error", "Task Failed",
                        e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Styled alert dialog
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        DialogPane dp = alert.getDialogPane();
        if (dp != null) {
            try {
                dp.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
                dp.getStyleClass().add("root");
            } catch (Exception e) {
                System.err.println("CSS load warning for dialog.");
            }
        }
        alert.showAndWait();
    }
}
