package controllers;

import app.Main;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import models.DataStore;
import models.SessionManager;
import models.User;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Login screen.
 * Handles credential validation, shake animation on failure,
 * and navigation to Dashboard or Register.
 */
public class LoginController implements Initializable {

    @FXML
    private VBox loginCard;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginBtn;
    @FXML
    private Label errorLabel;
    @FXML
    private Label loadingLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        loadingLabel.setVisible(false);

        // Slide-up entrance animation
        loginCard.setTranslateY(40);
        loginCard.setOpacity(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), loginCard);
        slide.setToY(0);
        FadeTransition fade = new FadeTransition(Duration.millis(600), loginCard);
        fade.setToValue(1.0);
        ParallelTransition intro = new ParallelTransition(slide, fade);
        intro.play();

        // Allow Enter key on password field
        passwordField.setOnAction(e -> handleLogin());
        emailField.setOnAction(e -> passwordField.requestFocus());
    }

    @FXML
    public void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        errorLabel.setVisible(false);

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter your email and password.");
            shakeCard();
            return;
        }

        // Show loading state
        loginBtn.setDisable(true);
        loginBtn.setText("Authenticating...");
        loadingLabel.setVisible(true);

        // Perform authentication on background thread
        new Thread(() -> {
            try {
                Thread.sleep(500); // Simulate auth delay
                User user = DataStore.getInstance().login(email, password);

                javafx.application.Platform.runLater(() -> {
                    if (user != null) {
                        DataStore.getInstance().setCurrentUser(user);
                        SessionManager.getInstance().createSession(user);
                        if (user.isAdmin()) {
                            Main.navigateTo("admin");
                        } else {
                            Main.navigateTo("dashboard");
                        }
                    } else {
                        loginBtn.setDisable(false);
                        loginBtn.setText("Sign In");
                        loadingLabel.setVisible(false);
                        showError("Invalid email or password. Please try again.");
                        shakeCard();
                        passwordField.clear();
                    }
                });
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @FXML
    public void handleDemoLogin() {
        emailField.setText("james@skyjet.com");
        passwordField.setText("password123");
        handleLogin();
    }

    @FXML
    public void handleAdminDemo() {
        emailField.setText("admin@skyjet.com");
        passwordField.setText("admin123");
        handleLogin();
    }

    @FXML
    public void goToRegister() {
        Main.navigateTo("register");
    }

    // ---- Helpers ----
    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    /** Horizontal shake animation on the login card */
    private void shakeCard() {
        TranslateTransition shake = new TranslateTransition(Duration.millis(60), loginCard);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.play();
    }
}
