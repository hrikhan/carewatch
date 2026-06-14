package com.carewatch.core.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;

// Scene switcher utility
public class SceneNavigator {
    private static Stage primaryStage;

    // Set primary stage
    public static void setStage(Stage stage) {
        primaryStage = stage;
    }

    // Navigate to FXML view
    public static void navigate(String fxmlPath, String title) {
        if (primaryStage == null) {
            System.err.println("Primary stage is not initialized in SceneNavigator!");
            return;
        }

        try {
            // Load FXML
            URL fxmlUrl = SceneNavigator.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("Cannot locate FXML file: " + fxmlPath);
            }
            
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            Scene scene = new Scene(root);
            
            // Apply dark theme CSS
            URL cssUrl = SceneNavigator.class.getResource("/styles/dark-theme.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.err.println("Warning: Could not load styles/dark-theme.css");
            }
            
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Failed to navigate to: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
