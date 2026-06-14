package com.carewatch;

import com.carewatch.core.navigation.SceneNavigator;
import javafx.application.Application;
import javafx.stage.Stage;

// Main entry point for CareWatch JavaFX app
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Init navigation stage
        SceneNavigator.setStage(primaryStage);
        
        // Set window size
        primaryStage.setWidth(960);
        primaryStage.setHeight(640);
        primaryStage.setMinWidth(480);
        primaryStage.setMinHeight(500);
        
        // Load login screen
        SceneNavigator.navigate("/views/auth/login.fxml", "CareWatch - Secure Sign In");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
