package controllers;

import app.Main;
import javafx.animation.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.util.Duration;
import models.DataStore;
import models.Flight;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for the Admin Dashboard.
 * Provides full CRUD UI for flight management and statistics overview.
 */
public class AdminController implements Initializable {

    // ---- Statistics Cards ----
    @FXML
    private Label statTotalFlights;
    @FXML
    private Label statTotalBookings;
    @FXML
    private Label statTotalRevenue;
    @FXML
    private Label statOccupancy;

    // ---- Flight Management Table ----
    @FXML
    private TableView<Flight> flightTable;
    @FXML
    private TableColumn<Flight, String> colFlightNum;
    @FXML
    private TableColumn<Flight, String> colAirline;
    @FXML
    private TableColumn<Flight, String> colOrigin;
    @FXML
    private TableColumn<Flight, String> colDestination;
    @FXML
    private TableColumn<Flight, String> colDeparture;
    @FXML
    private TableColumn<Flight, String> colArrival;
    @FXML
    private TableColumn<Flight, Double> colPrice;
    @FXML
    private TableColumn<Flight, Integer> colSeats;
    @FXML
    private TableColumn<Flight, String> colStatus;

    // ---- Add / Edit Form ----
    @FXML
    private TextField fldFlightNum;
    @FXML
    private TextField fldAirline;
    @FXML
    private TextField fldOrigin;
    @FXML
    private TextField fldDestination;
    @FXML
    private TextField fldDeparture;
    @FXML
    private TextField fldArrival;
    @FXML
    private TextField fldDuration;
    @FXML
    private TextField fldPrice;
    @FXML
    private TextField fldSeats;
    @FXML
    private TextField fldAircraft;
    @FXML
    private ComboBox<String> cmbStatus;
    @FXML
    private Button btnAddFlight;
    @FXML
    private Button btnUpdateFlight;
    @FXML
    private Button btnClearForm;
    @FXML
    private Label formStatusLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Verify user has admin access before loading page
            if (DataStore.getInstance().getCurrentUser() == null
                    || !DataStore.getInstance().getCurrentUser().isAdmin()) {
                Main.navigateTo("login");
                return;
            }

            configureTable();
            loadFlights();
            refreshStats();

            if (cmbStatus != null) {
                cmbStatus.getItems().addAll("ON TIME", "DELAYED", "CANCELLED", "BOARDING");
                cmbStatus.setValue("ON TIME");
            }

            if (formStatusLabel != null)
                formStatusLabel.setVisible(false);

            // Animate stats
            if (statTotalFlights != null)
                animateStatCard(statTotalFlights, DataStore.getInstance().getFlights().size());
            if (statTotalBookings != null)
                animateStatCard(statTotalBookings, DataStore.getInstance().getBookings().size());

            // Revenue from bookings
            double rev = DataStore.getInstance().getBookings().stream()
                    .mapToDouble(b -> b.getTotalCost()).sum();
            if (statTotalRevenue != null)
                statTotalRevenue.setText(String.format("$%,.0f", rev));
            if (statOccupancy != null)
                statOccupancy.setText("73%");

            // Click table row → populate form
            if (flightTable != null)
                flightTable.getSelectionModel().selectedItemProperty().addListener((obs, old, f) -> {
                    if (f != null)
                        populateFormWith(f);
                });
        } catch (Exception e) {
            System.err.println("Error initializing AdminController: " + e.getMessage());
            e.printStackTrace();
            Main.navigateTo("login");
        }
    }

    // ────────────────────────────────────────────
    // Table configuration
    // ────────────────────────────────────────────

    private void configureTable() {
        colFlightNum.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colAirline.setCellValueFactory(new PropertyValueFactory<>("airline"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departure"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrival"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double p, boolean empty) {
                super.updateItem(p, empty);
                setText(empty || p == null ? null : String.format("$%.0f", p));
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(s);
                String c = switch (s) {
                    case "DELAYED" -> "#f4a836";
                    case "CANCELLED" -> "#ff6b6b";
                    case "BOARDING" -> "#00b4d8";
                    default -> "#00d4aa";
                };
                setStyle("-fx-text-fill:" + c + "; -fx-font-weight:bold;");
            }
        });
    }

    private void loadFlights() {
        flightTable.setItems(DataStore.getInstance().getFlights());
    }

    private void refreshStats() {
        if (statTotalFlights != null)
            statTotalFlights.setText(String.valueOf(DataStore.getInstance().getFlights().size()));
        if (statTotalBookings != null)
            statTotalBookings.setText(String.valueOf(DataStore.getInstance().getBookings().size()));
    }

    // ────────────────────────────────────────────
    // CRUD Operations
    // ────────────────────────────────────────────

    /**
     * Verify user has admin privileges before allowing action
     * 
     * @return true if user is admin, false otherwise
     */
    private boolean verifyAdminAccess() {
        if (DataStore.getInstance().getCurrentUser() == null
                || !DataStore.getInstance().getCurrentUser().isAdmin()) {
            showFormStatus("❌ Access Denied: Admin privileges required.", false);
            Main.navigateTo("login");
            return false;
        }
        return true;
    }

    @FXML
    public void handleAddFlight() {
        if (!verifyAdminAccess())
            return;
        if (!validateForm())
            return;

        Flight f = buildFlightFromForm();
        try {
            DataStore.getInstance().addFlight(f);
            showFormStatus("Flight " + f.getFlightNumber() + " added successfully.", true);
            clearForm();
            loadFlights();
            refreshStats();
            animateStatCard(statTotalFlights, DataStore.getInstance().getFlights().size());
        } catch (RuntimeException ex) {
            showFormStatus("Could not add flight: " + ex.getMessage(), false);
        }
    }

    @FXML
    public void handleUpdateFlight() {
        if (!verifyAdminAccess())
            return;

        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showFormStatus("⚠  Select a flight from the table to update.", false);
            return;
        }
        if (!validateForm())
            return;

        selected.setFlightNumber(fldFlightNum.getText().trim().toUpperCase());
        selected.setAirline(fldAirline.getText().trim());
        selected.setOrigin(fldOrigin.getText().trim());
        selected.setDestination(fldDestination.getText().trim());
        selected.setDeparture(fldDeparture.getText().trim());
        selected.setArrival(fldArrival.getText().trim());
        selected.setDuration(fldDuration.getText().trim());
        selected.setPrice(Double.parseDouble(fldPrice.getText().trim()));
        selected.setAvailableSeats(Integer.parseInt(fldSeats.getText().trim()));
        selected.setAircraftType(fldAircraft.getText().trim());
        selected.setStatus(cmbStatus.getValue());

        try {
            DataStore.getInstance().updateFlight(selected);
            flightTable.refresh();
            showFormStatus("Flight updated successfully.", true);
        } catch (RuntimeException ex) {
            showFormStatus("Could not update flight: " + ex.getMessage(), false);
        }
    }

    @FXML
    public void handleDeleteFlight() {
        if (!verifyAdminAccess())
            return;

        Flight selected = flightTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showFormStatus("⚠  Select a flight from the table to delete.", false);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete flight " + selected.getFlightNumber() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Delete");
        confirm.setHeaderText(null);
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                DataStore.getInstance().removeFlight(selected);
                clearForm();
                refreshStats();
                showFormStatus("Flight deleted.", true);
                animateStatCard(statTotalFlights, DataStore.getInstance().getFlights().size());
            } catch (RuntimeException ex) {
                showFormStatus("Could not delete flight: " + ex.getMessage(), false);
            }
        }
    }

    @FXML
    public void clearForm() {
        fldFlightNum.clear();
        fldAirline.clear();
        fldOrigin.clear();
        fldDestination.clear();
        fldDeparture.clear();
        fldArrival.clear();
        fldDuration.clear();
        fldPrice.clear();
        fldSeats.clear();
        fldAircraft.clear();
        cmbStatus.setValue("ON TIME");
        flightTable.getSelectionModel().clearSelection();
        formStatusLabel.setVisible(false);
    }

    private void populateFormWith(Flight f) {
        fldFlightNum.setText(f.getFlightNumber());
        fldAirline.setText(f.getAirline());
        fldOrigin.setText(f.getOrigin());
        fldDestination.setText(f.getDestination());
        fldDeparture.setText(f.getDeparture());
        fldArrival.setText(f.getArrival());
        fldDuration.setText(f.getDuration());
        fldPrice.setText(String.valueOf(f.getPrice()));
        fldSeats.setText(String.valueOf(f.getAvailableSeats()));
        fldAircraft.setText(f.getAircraftType());
        cmbStatus.setValue(f.getStatus());
    }

    private Flight buildFlightFromForm() {
        return new Flight(
                fldFlightNum.getText().trim().toUpperCase(),
                fldAirline.getText().trim(),
                fldOrigin.getText().trim(),
                fldDestination.getText().trim(),
                fldDeparture.getText().trim(),
                fldArrival.getText().trim(),
                fldDuration.getText().trim(),
                Double.parseDouble(fldPrice.getText().trim()),
                Integer.parseInt(fldSeats.getText().trim()),
                fldAircraft.getText().trim(),
                cmbStatus.getValue());
    }

    private boolean validateForm() {
        if (fldFlightNum.getText().trim().isEmpty() ||
                fldOrigin.getText().trim().isEmpty() ||
                fldDestination.getText().trim().isEmpty() ||
                fldDeparture.getText().trim().isEmpty() ||
                fldPrice.getText().trim().isEmpty() ||
                fldSeats.getText().trim().isEmpty()) {
            showFormStatus("⚠  Please fill in all required fields.", false);
            return false;
        }
        try {
            Double.parseDouble(fldPrice.getText().trim());
            Integer.parseInt(fldSeats.getText().trim());
        } catch (NumberFormatException ex) {
            showFormStatus("⚠  Price and Seats must be numeric.", false);
            return false;
        }
        return true;
    }

    private void showFormStatus(String msg, boolean success) {
        formStatusLabel.setText(msg);
        formStatusLabel.setStyle(success
                ? "-fx-text-fill: #00d4aa;"
                : "-fx-text-fill: #ff6b6b;");
        formStatusLabel.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), formStatusLabel);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void animateStatCard(Label label, int target) {
        if (label == null)
            return;
        int steps = 30;
        Timeline tl = new Timeline();
        for (int i = 0; i <= steps; i++) {
            final int val = (int) Math.round((double) target * i / steps);
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(i * 18L),
                    e -> label.setText(String.valueOf(val))));
        }
        tl.play();
    }

    // ---- Sidebar Navigation ----
    @FXML
    public void goToDashboard() {
        Main.navigateTo("dashboard");
    }

    @FXML
    public void goToSearch() {
        Main.navigateTo("search");
    }

    @FXML
    public void goToBookings() {
        Main.navigateTo("booking");
    }

    @FXML
    public void goToAdmin() {
        /* already here */ }

    @FXML
    public void handleLogout() {
        DataStore.getInstance().setCurrentUser(null);
        Main.navigateTo("login");
    }
}
