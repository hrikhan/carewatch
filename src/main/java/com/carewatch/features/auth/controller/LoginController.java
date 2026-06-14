package com.carewatch.features.auth.controller;

import com.carewatch.core.navigation.SceneNavigator;
import com.carewatch.core.session.SessionManager;
import com.carewatch.features.auth.service.AuthService;
import com.carewatch.features.user.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.sql.SQLException;

// Controller for Login Screen
public class LoginController {
    
    private final AuthService authService = new AuthService();

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private VBox cardBox;
    @FXML
    private StackPane loadingOverlay;

    // Initialize controller
    @FXML
    public void initialize() {
        errorLabel.setText("");
    }

    // Handle user login asynchronously
    @FXML
    private void handleLogin() {
        String email = emailField.getText();
        String password = passwordField.getText();

        errorLabel.setText("");
        loadingOverlay.setVisible(true);
        cardBox.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                User user = authService.login(email, password);
                Thread.sleep(400); // Latency simulation for spinner

                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    cardBox.setDisable(false);

                    if (user != null) {
                        SessionManager.startSession(user);

                        // Redirect based on role
                        String role = user.getRole();
                        if ("PATIENT".equalsIgnoreCase(role)) {
                            SceneNavigator.navigate("/views/dashboard/patient_dashboard.fxml", "CareWatch - Patient Portal");
                        } else if ("DOCTOR_ADMIN".equalsIgnoreCase(role)) {
                            SceneNavigator.navigate("/views/dashboard/doctor_dashboard.fxml", "CareWatch - Doctor Portal");
                        } else if ("SUPER_ADMIN".equalsIgnoreCase(role)) {
                            SceneNavigator.navigate("/views/dashboard/super_admin_dashboard.fxml", "CareWatch - System Management");
                        } else {
                            errorLabel.setText("Error: Authorized role is unrecognized.");
                        }
                    } else {
                        errorLabel.setText("Invalid email or password.");
                    }
                });
            } catch (IllegalArgumentException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    cardBox.setDisable(false);
                    errorLabel.setText(e.getMessage());
                });
            } catch (SQLException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    cardBox.setDisable(false);
                    errorLabel.setText("DB offline. Use demo accounts (e.g. patient@gmail.com / 123456)");
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    loadingOverlay.setVisible(false);
                    cardBox.setDisable(false);
                });
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
