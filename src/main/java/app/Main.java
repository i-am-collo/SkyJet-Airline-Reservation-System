package app;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Objects;

/**
 * SkyJet Airline Reservation System - Main Entry Point
 * JavaFX Application with smooth scene transitions and modern UI
 */
public class Main extends Application {

    private static Stage primaryStage;
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 800;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Configure stage
        primaryStage.setTitle("SkyJet - Premium Airline Reservation");
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(DEFAULT_WIDTH);
        primaryStage.setHeight(DEFAULT_HEIGHT);

        // Try to set icon (graceful fallback if asset missing)
        try {
            primaryStage.getIcons().add(
                    new Image(Objects.requireNonNull(
                            Main.class.getResourceAsStream("/assets/logo.png"))));
        } catch (Exception ignored) {
        }

        // Load login screen
        loadScene("login");
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    /**
     * Switch to a named scene with a fade transition.
     * 
     * @param sceneName name matching the FXML file (without .fxml)
     */
    public static void navigateTo(String sceneName) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    Main.class.getResource("/views/" + sceneName + ".fxml"));
            Parent root = loader.load();

            final Scene scene = primaryStage.getScene();
            if (scene == null) {
                final Scene newScene = new Scene(root);
                newScene.getStylesheets().add(
                        Objects.requireNonNull(
                                Main.class.getResource("/styles/style.css")).toExternalForm());
                primaryStage.setScene(newScene);
            } else {
                // Fade out, swap root, then fade in
                Parent oldRoot = scene.getRoot();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), oldRoot);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    scene.setRoot(root);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /** First-load helper used in start() */
    private static void loadScene(String sceneName) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/views/" + sceneName + ".fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                Objects.requireNonNull(
                        Main.class.getResource("/styles/style.css")).toExternalForm());
        primaryStage.setScene(scene);

        // Initial fade-in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
