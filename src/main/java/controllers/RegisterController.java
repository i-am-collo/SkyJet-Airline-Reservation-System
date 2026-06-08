package controllers;

import app.Main;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import models.DataStore;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Controller for the Register screen.
 * Performs client-side validation before "creating" the account.
 */
public class RegisterController implements Initializable {

    @FXML private VBox          registerCard;
    @FXML private TextField     nameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button        registerBtn;
    @FXML private Label         errorLabel;
    @FXML private Label         successLabel;

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        successLabel.setVisible(false);

        // Slide-up entrance
        registerCard.setTranslateY(40);
        registerCard.setOpacity(0);
        TranslateTransition slide = new TranslateTransition(Duration.millis(600), registerCard);
        slide.setToY(0);
        FadeTransition fade = new FadeTransition(Duration.millis(600), registerCard);
        fade.setToValue(1.0);
        new ParallelTransition(slide, fade).play();
    }

    @FXML
    public void handleRegister() {
        String name     = nameField.getText().trim();
        String email    = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm  = confirmPasswordField.getText();

        errorLabel.setVisible(false);
        successLabel.setVisible(false);

        // Validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        if (name.length() < 3) {
            showError("Full name must be at least 3 characters.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showError("Please enter a valid email address.");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        registerBtn.setDisable(true);
        registerBtn.setText("Creating Account...");

        PauseTransition pause = new PauseTransition(Duration.millis(1000));
        pause.setOnFinished(e -> {
            String error = DataStore.getInstance().registerUser(name, email, password);
            if (error == null) {
                successLabel.setText("Account created! Redirecting to login...");
                successLabel.setVisible(true);
                PauseTransition redirect = new PauseTransition(Duration.millis(1500));
                redirect.setOnFinished(ev -> Main.navigateTo("login"));
                redirect.play();
            } else {
                registerBtn.setDisable(false);
                registerBtn.setText("Create Account");
                showError(error);
            }
        });
        pause.play();
    }

    @FXML
    public void goToLogin() {
        Main.navigateTo("login");
    }

    private void showError(String msg) {
        errorLabel.setText("⚠  " + msg);
        errorLabel.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), errorLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }
}
