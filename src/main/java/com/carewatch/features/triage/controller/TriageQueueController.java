package com.carewatch.features.triage.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.core.session.SessionManager;
import com.carewatch.features.triage.model.TriageItem;
import com.carewatch.features.triage.service.TriageService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Controller for managing the patient clinical triage queue
public class TriageQueueController {

    private final TriageService triageService = new TriageService();
    private final ObservableList<TriageItem> triageList = FXCollections.observableArrayList();

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private Button btnResolve;

    @FXML
    private TableView<TriageItem> triageTable;
    @FXML
    private TableColumn<TriageItem, Integer> idColumn;
    @FXML
    private TableColumn<TriageItem, String> nameColumn;
    @FXML
    private TableColumn<TriageItem, String> emailColumn;
    @FXML
    private TableColumn<TriageItem, String> phoneColumn;
    @FXML
    private TableColumn<TriageItem, String> diseaseColumn;
    @FXML
    private TableColumn<TriageItem, String> urgencyColumn;
    @FXML
    private TableColumn<TriageItem, Integer> priorityColumn;
    @FXML
    private TableColumn<TriageItem, String> alertColumn;
    @FXML
    private TableColumn<TriageItem, String> statusColumn;
    @FXML
    private TableColumn<TriageItem, String> dateColumn;

    @FXML
    private StackPane loadingOverlay;

    @FXML
    public void initialize() {
        // Map TableView columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("queueId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("patientName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        diseaseColumn.setCellValueFactory(new PropertyValueFactory<>("diseaseType"));
        urgencyColumn.setCellValueFactory(new PropertyValueFactory<>("urgencyLevel"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priorityScore"));
        alertColumn.setCellValueFactory(new PropertyValueFactory<>("alertMessage"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("formattedDate"));

        // Row highlighting based on priority and resolution
        triageTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(TriageItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else {
                    String status = item.getStatus();
                    String urgency = item.getUrgencyLevel();
                    
                    if ("RESOLVED".equalsIgnoreCase(status)) {
                        setStyle("-fx-background-color: rgba(139, 148, 158, 0.12);"); // Soft grey/green resolved row tint
                    } else if ("CRITICAL".equalsIgnoreCase(urgency)) {
                        setStyle("-fx-background-color: rgba(248, 81, 73, 0.15);"); // Light Red critical row tint
                    } else if ("WARNING".equalsIgnoreCase(urgency)) {
                        setStyle("-fx-background-color: rgba(240, 136, 59, 0.15);"); // Light Orange warning row tint
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        // Urgency column cell coloring
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

        // Status column cell coloring
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PENDING".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ffb454; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #8b949e; -fx-font-weight: bold;");
                    }
                }
            }
        });

        triageTable.setItems(triageList);

        // Populate status combo options
        statusComboBox.setItems(FXCollections.observableArrayList("ALL", "PENDING", "RESOLVED"));
        statusComboBox.setValue("ALL");

        // Load data
        loadTriageQueue(true);
    }

    // Load triage queue data asynchronously
    private void loadTriageQueue(boolean showLoader) {
        String keyword = searchField.getText();
        String status = statusComboBox.getValue();

        if (showLoader) {
            loadingOverlay.setVisible(true);
            triageTable.setDisable(true);
        }

        Thread thread = new Thread(() -> {
            try {
                List<TriageItem> queue = triageService.searchAndFilterQueue(keyword, status);
                Thread.sleep(200); // UI visual transition delay

                Platform.runLater(() -> {
                    triageList.setAll(queue);
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                        triageTable.setDisable(false);
                    }
                });

            } catch (SQLException e) {
                Platform.runLater(() -> {
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                        triageTable.setDisable(false);
                    }
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to Load Triage Queue", e.getMessage());
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                        triageTable.setDisable(false);
                    }
                    showAlert(Alert.AlertType.ERROR, "System Error", "Load Operation Failed", e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Handles keystrokes in search text area
    @FXML
    private void handleSearchKeyPress() {
        loadTriageQueue(false);
    }

    // Handles search button click action
    @FXML
    private void handleSearch() {
        loadTriageQueue(true);
    }

    // Handles status dropdown value changes
    @FXML
    private void handleFilter() {
        loadTriageQueue(true);
    }

    // Refresh button click action
    @FXML
    private void handleRefreshManual() {
        loadTriageQueue(true);
    }

    // Back navigation to Doctor dashboard
    @FXML
    private void handleBack() {
        SceneNavigator.navigate("/views/dashboard/doctor_dashboard.fxml", "CareWatch - Doctor Portal");
    }

    // Marks selected triage queue item as RESOLVED
    @FXML
    private void handleResolve() {
        TriageItem selectedItem = triageTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "No Item Selected", "Please select a triage patient entry from the list to resolve.");
            return;
        }

        if ("RESOLVED".equalsIgnoreCase(selectedItem.getStatus())) {
            showAlert(Alert.AlertType.INFORMATION, "Information", "Already Resolved", "This triage queue item has already been marked as RESOLVED.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Resolve Triage Alert");
        confirmAlert.setHeaderText("Resolve Case for " + selectedItem.getPatientName());
        confirmAlert.setContentText("Are you sure you want to mark this patient triage case as RESOLVED?");

        // Style the confirmation alert
        DialogPane pane = confirmAlert.getDialogPane();
        if (pane != null) {
            try {
                pane.getStylesheets().add(getClass().getResource("/styles/dark-theme.css").toExternalForm());
                pane.getStyleClass().add("root");
            } catch (Exception e) {
                System.err.println("Warning: CSS could not be loaded for dialog.");
            }
        }

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            loadingOverlay.setVisible(true);
            triageTable.setDisable(true);

            Thread thread = new Thread(() -> {
                try {
                    triageService.resolveTriageItem(selectedItem.getQueueId());
                    Thread.sleep(300); // Visual completion delay

                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Triage Alert Resolved", "The patient's triage case has been successfully resolved.");
                        loadTriageQueue(true);
                    });

                } catch (SQLException e) {
                    Platform.runLater(() -> {
                        loadingOverlay.setVisible(false);
                        triageTable.setDisable(false);
                        showAlert(Alert.AlertType.ERROR, "Database Error", "Action Failed", e.getMessage());
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        loadingOverlay.setVisible(false);
                        triageTable.setDisable(false);
                        showAlert(Alert.AlertType.ERROR, "System Error", "Action Failed", e.getMessage());
                    });
                }
            });
            thread.setDaemon(true);
            thread.start();
        }
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
