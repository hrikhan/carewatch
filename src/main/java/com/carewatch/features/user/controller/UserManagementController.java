package com.carewatch.features.user.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.features.user.model.User;
import com.carewatch.features.user.service.UserService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

// Controller for User Management view
public class UserManagementController {

    private final UserService userService = new UserService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private ComboBox<String> statusComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> roleFilterComboBox;

    @FXML
    private TableView<User> userTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> statusColumn;
    @FXML
    private TableColumn<User, Timestamp> dateColumn;

    @FXML
    private StackPane loadingOverlay;

    // Initialize controller and set up bindings
    @FXML
    public void initialize() {
        roleComboBox.setItems(FXCollections.observableArrayList("PATIENT", "DOCTOR_ADMIN", "SUPER_ADMIN"));
        statusComboBox.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
        roleFilterComboBox.setItems(FXCollections.observableArrayList("ALL", "PATIENT", "DOCTOR_ADMIN", "SUPER_ADMIN"));
        roleFilterComboBox.setValue("ALL");

        idColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        userTable.setItems(userList);

        // Populate form fields on select
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fullNameField.setText(newVal.getFullName());
                emailField.setText(newVal.getEmail());
                roleComboBox.setValue(newVal.getRole());
                statusComboBox.setValue(newVal.getStatus());
                passwordField.setText(newVal.getPassword());
            }
        });

        loadData(true);
    }

    // Load user records asynchronously
    private void loadData(boolean showLoader) {
        if (showLoader) {
            loadingOverlay.setVisible(true);
        }
        Thread thread = new Thread(() -> {
            try {
                List<User> allUsers = userService.getAllUsers();
                Platform.runLater(() -> {
                    roleFilterComboBox.setValue("ALL");
                    searchField.clear();
                    userList.setAll(allUsers);
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                    }
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    if (showLoader) {
                        loadingOverlay.setVisible(false);
                    }
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Load Failed", "Could not fetch user list: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Add new user
    @FXML
    private void handleAddUser() {
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String role = roleComboBox.getValue();
        String status = statusComboBox.getValue();
        String password = passwordField.getText();

        loadingOverlay.setVisible(true);

        Thread thread = new Thread(() -> {
            try {
                User newUser = new User();
                newUser.setFullName(fullName);
                newUser.setEmail(email);
                newUser.setRole(role);
                newUser.setStatus(status);

                userService.createUser(newUser, password);

                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User Created", "New user has been registered successfully.");
                    handleClear();
                    loadData(true);
                });
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to create user", "A database error occurred: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Update status of selected user
    @FXML
    private void handleUpdateStatus() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert(Alert.AlertType.WARNING, "Selection Required", "No User Selected", "Please select a user from the table to update status.");
            return;
        }

        String newStatus = statusComboBox.getValue();
        if (newStatus == null) {
            showAlert(Alert.AlertType.WARNING, "Input Required", "No Status Selected", "Please choose a status from the status dropdown.");
            return;
        }

        loadingOverlay.setVisible(true);
        int userId = selectedUser.getUserId();

        Thread thread = new Thread(() -> {
            try {
                userService.updateUserStatus(userId, newStatus);
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Status Updated", "User status updated successfully.");
                    loadData(true);
                });
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.WARNING, "Validation Error", "Invalid Input", e.getMessage());
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Unable to update status", "A database error occurred: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Clear all inputs
    @FXML
    private void handleClear() {
        fullNameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.setValue(null);
        statusComboBox.setValue(null);
        userTable.getSelectionModel().clearSelection();
    }

    // Redirect back to Super Admin Dashboard
    @FXML
    private void handleBack() {
        SceneNavigator.navigate("/views/dashboard/super_admin_dashboard.fxml", "CareWatch - System Management");
    }

    // Search users by name/email keyword
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        loadingOverlay.setVisible(true);

        Thread thread = new Thread(() -> {
            try {
                List<User> results = userService.searchUsers(keyword);
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    userList.setAll(results);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Search Failed", "Could not complete search: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Filter users by role select
    @FXML
    private void handleFilter() {
        String roleFilter = roleFilterComboBox.getValue();
        loadingOverlay.setVisible(true);

        Thread thread = new Thread(() -> {
            try {
                List<User> results = userService.filterUsersByRole(roleFilter);
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    userList.setAll(results);
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Filter Failed", "Could not complete filter: " + e.getMessage());
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // Refresh user list TableView
    @FXML
    private void handleRefresh() {
        loadData(true);
    }

    // Display alert message popup
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
                System.err.println("Warning: CSS could not be applied to Alert Dialog.");
            }
        }
        alert.showAndWait();
    }
}
