package com.carewatch.features.vitals.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.core.session.SessionManager;
import com.carewatch.features.user.model.User;
import com.carewatch.features.vitals.model.Vital;
import com.carewatch.features.vitals.service.VitalsService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

// Controller for viewing patient vitals history with search/filter and loading indicators
public class VitalsHistoryController {

    private final VitalsService vitalsService = new VitalsService();
    private final ObservableList<Vital> allVitals = FXCollections.observableArrayList();
    private final ObservableList<Vital> filteredVitals = FXCollections.observableArrayList();

    @FXML private TableView<Vital> vitalsTable;
    @FXML private TableColumn<Vital, String>  dateColumn;
    @FXML private TableColumn<Vital, Integer> heartRateColumn;
    @FXML private TableColumn<Vital, Integer> oxygenColumn;
    @FXML private TableColumn<Vital, String>  bpColumn;
    @FXML private TableColumn<Vital, Double>  tempColumn;
    @FXML private TableColumn<Vital, Integer> sugarColumn;
    @FXML private TableColumn<Vital, String>  urgencyColumn;
    @FXML private TableColumn<Vital, String>  symptomsColumn;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> urgencyFilterComboBox;
    @FXML private Label recordCountLabel;
    @FXML private StackPane loadingOverlay;

    @FXML
    public void initialize() {
        // Map columns to Vital model properties
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));
        heartRateColumn.setCellValueFactory(new PropertyValueFactory<>("heartRate"));
        oxygenColumn.setCellValueFactory(new PropertyValueFactory<>("oxygenLevel"));
        bpColumn.setCellValueFactory(new PropertyValueFactory<>("bp"));
        tempColumn.setCellValueFactory(new PropertyValueFactory<>("temperature"));
        sugarColumn.setCellValueFactory(new PropertyValueFactory<>("sugarLevel"));
        urgencyColumn.setCellValueFactory(new PropertyValueFactory<>("urgencyLevel"));
        symptomsColumn.setCellValueFactory(new PropertyValueFactory<>("symptoms"));

        // Row factory — color-code rows by urgency level
        vitalsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Vital item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-critical", "row-warning", "row-normal");
                if (empty || item == null) {
                    setStyle("");
                } else {
                    String level = item.getUrgencyLevel();
                    if ("CRITICAL".equalsIgnoreCase(level)) {
                        setStyle("-fx-background-color: rgba(248, 81, 73, 0.12);");
                    } else if ("WARNING".equalsIgnoreCase(level)) {
                        setStyle("-fx-background-color: rgba(240, 136, 59, 0.12);");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Urgency column — colored badge-like text
        urgencyColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(null);
                setGraphic(null);
                setStyle("");
                if (!empty && item != null) {
                    // Create a styled badge label
                    Label badge = new Label(item);
                    badge.setStyle(getBadgeStyle(item));
                    setGraphic(badge);
                }
            }

            private String getBadgeStyle(String level) {
                return switch (level.toUpperCase()) {
                    case "CRITICAL" -> "-fx-text-fill: #ff7b72; -fx-font-weight: bold; -fx-background-color: rgba(248,81,73,0.18); -fx-padding: 3px 10px; -fx-background-radius: 4px; -fx-border-color: rgba(248,81,73,0.4); -fx-border-width: 1px; -fx-border-radius: 4px;";
                    case "WARNING"  -> "-fx-text-fill: #f0883b; -fx-font-weight: bold; -fx-background-color: rgba(240,136,59,0.18); -fx-padding: 3px 10px; -fx-background-radius: 4px; -fx-border-color: rgba(240,136,59,0.4); -fx-border-width: 1px; -fx-border-radius: 4px;";
                    default         -> "-fx-text-fill: #56d364; -fx-font-weight: bold; -fx-background-color: rgba(86,211,100,0.18); -fx-padding: 3px 10px; -fx-background-radius: 4px; -fx-border-color: rgba(86,211,100,0.4); -fx-border-width: 1px; -fx-border-radius: 4px;";
                };
            }
        });

        // Heart rate column — color threshold coloring
        heartRateColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item + " bpm");
                if (item < 50 || item > 100) {
                    setStyle("-fx-text-fill: #ff7b72; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #56d364;");
                }
            }
        });

        // Oxygen column — color below 95%
        oxygenColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item + "%");
                if (item < 95) {
                    setStyle("-fx-text-fill: #ff7b72; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #56d364;");
                }
            }
        });

        // Temperature column — color out-of-range
        tempColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item + "°F");
                if (item < 97.0 || item > 99.5) {
                    setStyle("-fx-text-fill: #f0883b; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #c9d1d9;");
                }
            }
        });

        vitalsTable.setItems(filteredVitals);

        // Setup urgency filter combo box
        urgencyFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "NORMAL", "WARNING", "CRITICAL"));
        urgencyFilterComboBox.setValue("ALL");

        // Initial data load
        handleRefresh();
    }

    // Refresh data from database
    @FXML
    private void handleRefresh() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) { handleBack(); return; }

        loadingOverlay.setVisible(true);
        vitalsTable.setDisable(true);
        if (recordCountLabel != null) recordCountLabel.setText("Loading...");

        Thread thread = new Thread(() -> {
            try {
                int patientId = vitalsService.getPatientIdByUserId(currentUser.getUserId());
                List<Vital> history = vitalsService.getMyVitalsHistory(patientId);
                Thread.sleep(300);

                Platform.runLater(() -> {
                    allVitals.setAll(history);
                    applyFilters();
                    loadingOverlay.setVisible(false);
                    vitalsTable.setDisable(false);
                });

            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    vitalsTable.setDisable(false);
                    if (recordCountLabel != null) recordCountLabel.setText("No profile found");
                    showAlert(Alert.AlertType.WARNING, "Profile Required", "No Patient Profile Found", e.getMessage());
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    vitalsTable.setDisable(false);
                    if (recordCountLabel != null) recordCountLabel.setText("Error loading data");
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to Load Vitals History", e.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    vitalsTable.setDisable(false);
                    showAlert(Alert.AlertType.ERROR, "Error", "Unexpected Exception", e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Apply search + urgency filter on already-loaded data (no extra DB call)
    private void applyFilters() {
        String keyword = searchField != null ? searchField.getText().toLowerCase().trim() : "";
        String urgency = urgencyFilterComboBox != null ? urgencyFilterComboBox.getValue() : "ALL";

        List<Vital> result = allVitals.stream()
            .filter(v -> {
                boolean matchesKeyword = keyword.isEmpty()
                    || (v.getFormattedDate() != null && v.getFormattedDate().toLowerCase().contains(keyword))
                    || (v.getSymptoms() != null && v.getSymptoms().toLowerCase().contains(keyword))
                    || (v.getBp() != null && v.getBp().toLowerCase().contains(keyword));
                boolean matchesUrgency = "ALL".equalsIgnoreCase(urgency)
                    || (v.getUrgencyLevel() != null && v.getUrgencyLevel().equalsIgnoreCase(urgency));
                return matchesKeyword && matchesUrgency;
            })
            .collect(Collectors.toList());

        filteredVitals.setAll(result);

        // Update record count label
        if (recordCountLabel != null) {
            int total = allVitals.size();
            int shown = result.size();
            if (shown == total) {
                recordCountLabel.setText(total + " record" + (total != 1 ? "s" : ""));
            } else {
                recordCountLabel.setText(shown + " of " + total + " shown");
            }
        }
    }

    // Live search on keypress — filter in-memory
    @FXML
    private void handleSearchKeyPress() {
        applyFilters();
    }

    // Search button click
    @FXML
    private void handleSearch() {
        applyFilters();
    }

    // Urgency filter change
    @FXML
    private void handleFilter() {
        applyFilters();
    }

    // Clear all filters and reset view
    @FXML
    private void handleClearFilters() {
        if (searchField != null) searchField.clear();
        if (urgencyFilterComboBox != null) urgencyFilterComboBox.setValue("ALL");
        applyFilters();
    }

    // Navigate back to patient dashboard
    @FXML
    private void handleBack() {
        SceneNavigator.navigate("/views/dashboard/patient_dashboard.fxml", "CareWatch - Patient Portal");
    }

    // Navigate to vitals submission form
    @FXML
    private void handleShowSubmitForm() {
        SessionManager.setPendingFragment("/views/vitals/submit_vitals.fxml");
        SceneNavigator.navigate("/views/dashboard/patient_dashboard.fxml", "CareWatch - Patient Portal");
    }

    // Styled alert dialog helper
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
