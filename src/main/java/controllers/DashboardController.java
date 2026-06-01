package controllers;

import app.Main;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import models.DataStore;
import models.Flight;
import models.User;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Controller for the main Dashboard screen.
 * Populates the welcome section, statistics cards, and quick-action buttons.
 */
public class DashboardController implements Initializable {

    // Top bar
    @FXML private Label  userNameLabel;
    @FXML private Label  userInitialsLabel;
    @FXML private Label  userRoleLabel;
    @FXML private Label  notificationBadge;

    // Stats cards
    @FXML private Label  statFlightsValue;
    @FXML private Label  statBookingsValue;
    @FXML private Label  statSeatsValue;
    @FXML private Label  statRoutesValue;

    // Welcome
    @FXML private Label  welcomeLabel;
    @FXML private Label  welcomeSubLabel;

    // Content area
    @FXML private VBox   mainContent;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        User user = DataStore.getInstance().getCurrentUser();

        if (user != null) {
            welcomeLabel.setText("Welcome back, " + user.getFullName().split(" ")[0] + "!");
            welcomeSubLabel.setText("Member since " + user.getJoinDate()
                + "  ·  " + user.getMemberTier() + " Member");
            userNameLabel.setText(user.getFullName());
            userRoleLabel.setText(user.getMemberTier() + " Member");
            userInitialsLabel.setText(user.getAvatarInitials());
        } else {
            welcomeLabel.setText("Welcome to SkyJet!");
            welcomeSubLabel.setText("Your premium flight booking platform");
            userNameLabel.setText("Guest");
            userRoleLabel.setText("Guest");
            userInitialsLabel.setText("SJ");
        }

        var flights = DataStore.getInstance().getFlights();
        int availableSeats = flights.stream().mapToInt(Flight::getAvailableSeats).sum();
        Set<String> routes = new HashSet<>();
        for (Flight flight : flights) {
            routes.add(flight.getOrigin() + " -> " + flight.getDestination());
        }

        // Animate statistics counters
        animateCounter(statFlightsValue, 0, flights.size(), "");
        animateCounter(statBookingsValue, 0, DataStore.getInstance().getBookings().size(), "");
        animateCounter(statSeatsValue, 0, availableSeats, "");
        animateCounter(statRoutesValue, 0, routes.size(), "");
    }

    /** Count-up animation for stat cards */
    private void animateCounter(Label label, int from, int to, String suffix) {
        int steps = 40;
        Duration stepDuration = Duration.millis(20);
        int[] current = {from};
        int increment = Math.max(1, (to - from) / steps);

        Timeline tl = new Timeline();
        for (int i = 0; i <= steps; i++) {
            final int val = Math.min(from + i * increment, to);
            KeyFrame kf = new KeyFrame(stepDuration.multiply(i),
                e -> label.setText(String.valueOf(val) + suffix));
            tl.getKeyFrames().add(kf);
        }
        // Ensure final exact value
        tl.getKeyFrames().add(new KeyFrame(stepDuration.multiply(steps + 1),
            e -> label.setText(String.valueOf(to) + suffix)));
        tl.play();
    }

    // ---- Sidebar Navigation ----
    @FXML public void goToDashboard()  { /* already here */ }
    @FXML public void goToSearch()     { Main.navigateTo("search"); }
    @FXML public void goToBookings()   { Main.navigateTo("booking"); }
    @FXML public void goToAdmin()      { Main.navigateTo("admin"); }
    @FXML public void handleLogout()   {
        DataStore.getInstance().setCurrentUser(null);
        Main.navigateTo("login");
    }
    @FXML public void quickSearch()    { Main.navigateTo("search"); }
    @FXML public void viewHistory()    { Main.navigateTo("booking"); }
}
