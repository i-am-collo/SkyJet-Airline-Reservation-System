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
import models.Booking;
import models.DataStore;
import models.Flight;
import models.LoginAudit;

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

    @FXML
    private TableView<Booking> adminBookingsTable;
    @FXML
    private TableColumn<Booking, String> adminColBookingRef;
    @FXML
    private TableColumn<Booking, String> adminColPassenger;
    @FXML
    private TableColumn<Booking, String> adminColBookingFlight;
    @FXML
    private TableColumn<Booking, String> adminColBookingRoute;
    @FXML
    private TableColumn<Booking, String> adminColBookingSeat;
    @FXML
    private TableColumn<Booking, Double> adminColBookingCost;
    @FXML
    private TableColumn<Booking, String> adminColBookingStatus;
    @FXML
    private TableColumn<Booking, String> adminColBookingDate;

    @FXML
    private TableView<LoginAudit> auditTable;
    @FXML
    private TableColumn<LoginAudit, String> auditColEmail;
    @FXML
    private TableColumn<LoginAudit, String> auditColStatus;
    @FXML
    private TableColumn<LoginAudit, String> auditColTime;

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
            configureAdminTables();
            loadFlights();
            loadAdminTables();
            refreshStats();

            if (cmbStatus != null) {
                cmbStatus.getItems().addAll("ON TIME", "DELAYED", "CANCELLED", "BOARDING");
                cmbStatus.setValue("ON TIME");
            }

            if (formStatusLabel != null)
                formStatusLabel.setVisible(false);

            // Animate stats
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

    private void configureAdminTables() {
        if (adminBookingsTable != null) {
            adminColBookingRef.setCellValueFactory(new PropertyValueFactory<>("bookingRef"));
            adminColPassenger.setCellValueFactory(new PropertyValueFactory<>("passengerName"));
            adminColBookingFlight.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
            adminColBookingRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
            adminColBookingSeat.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
            adminColBookingCost.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
            adminColBookingStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
            adminColBookingDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));
            adminColBookingCost.setCellFactory(col -> new TableCell<>() {
                @Override
                protected void updateItem(Double cost, boolean empty) {
                    super.updateItem(cost, empty);
                    setText(empty || cost == null ? null : String.format("$%.2f", cost));
                }
            });
        }

        if (auditTable != null) {
            auditColEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
            auditColStatus.setCellValueFactory(new PropertyValueFactory<>("reason"));
            auditColTime.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        }
    }

    private void loadAdminTables() {
        if (adminBookingsTable != null) {
            adminBookingsTable.setItems(DataStore.getInstance().getAdminBookings());
        }
        if (auditTable != null) {
            auditTable.setItems(DataStore.getInstance().getBackendLoginAudits());
        }
    }

    private void refreshStats() {
        ObservableList<Flight> flights = DataStore.getInstance().getFlights();
        ObservableList<Booking> bookings = DataStore.getInstance().getAdminBookings();
        long confirmedBookings = bookings.stream()
                .filter(b -> "CONFIRMED".equalsIgnoreCase(b.getStatus()))
                .count();
        double revenue = bookings.stream()
                .filter(b -> !"CANCELLED".equalsIgnoreCase(b.getStatus()))
                .mapToDouble(Booking::getTotalCost)
                .sum();
        int availableSeats = flights.stream().mapToInt(Flight::getAvailableSeats).sum();
        long totalSeatDemand = availableSeats + confirmedBookings;
        long occupancy = totalSeatDemand == 0 ? 0 : Math.round((confirmedBookings * 100.0) / totalSeatDemand);

        if (statTotalFlights != null)
            statTotalFlights.setText(String.valueOf(flights.size()));
        if (statTotalBookings != null)
            statTotalBookings.setText(String.valueOf(bookings.size()));
        if (statTotalRevenue != null)
            statTotalRevenue.setText(String.format("$%,.0f", revenue));
        if (statOccupancy != null)
            statOccupancy.setText(occupancy + "%");
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
            loadAdminTables();
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
            loadAdminTables();
            refreshStats();
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
                loadAdminTables();
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
