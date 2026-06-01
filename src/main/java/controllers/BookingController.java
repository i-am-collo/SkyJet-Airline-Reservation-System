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
import models.User;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Controller for the Booking Confirmation screen AND Booking History.
 * Shows flight/seat summary, collects passenger details, confirms booking,
 * and displays all past bookings in a TableView.
 */
public class BookingController implements Initializable {

    // ---- Booking Summary ----
    @FXML private Label  summaryFlightNum;
    @FXML private Label  summaryRoute;
    @FXML private Label  summaryDeparture;
    @FXML private Label  summaryAircraft;
    @FXML private Label  summarySeat;
    @FXML private Label  summaryClass;
    @FXML private Label  summaryBasePrice;
    @FXML private Label  summaryTax;
    @FXML private Label  summaryTotal;

    // ---- Passenger Form ----
    @FXML private TextField   passengerNameField;
    @FXML private TextField   passengerEmailField;
    @FXML private TextField   passengerPassportField;
    @FXML private ComboBox<String> nationalityCombo;
    @FXML private DatePicker  dobPicker;
    @FXML private ComboBox<String> mealCombo;

    // ---- Confirm ----
    @FXML private Button  confirmBtn;
    @FXML private VBox    confirmationPane;  // shown after booking
    @FXML private Label   bookingRefLabel;
    @FXML private VBox    formPane;

    // ---- History Table ----
    @FXML private TableView<Booking>            historyTable;
    @FXML private TableColumn<Booking, String>  colRef;
    @FXML private TableColumn<Booking, String>  colFlight;
    @FXML private TableColumn<Booking, String>  colRoute;
    @FXML private TableColumn<Booking, String>  colDepart;
    @FXML private TableColumn<Booking, String>  colSeat;
    @FXML private TableColumn<Booking, String>  colClass;
    @FXML private TableColumn<Booking, Double>  colCost;
    @FXML private TableColumn<Booking, String>  colStatus;
    @FXML private TableColumn<Booking, String>  colDate;

    // Computed pricing
    private double basePrice = 0;
    private double tax       = 0;
    private double total     = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        populateForm();
        configureHistoryTable();
        loadHistory();

        // Show form, hide confirmation banner
        if (formPane != null)          formPane.setVisible(true);
        if (confirmationPane != null)  confirmationPane.setVisible(false);
    }

    // ────────────────────────────────────────────
    //  Populate summary from DataStore
    // ────────────────────────────────────────────

    private void populateForm() {
        Flight f    = DataStore.getInstance().getSelectedFlight();
        String seat = DataStore.getInstance().getSelectedSeat();
        User   user = DataStore.getInstance().getCurrentUser();

        // Pre-fill name/email from logged-in user
        if (user != null && passengerNameField != null) {
            passengerNameField.setText(user.getFullName());
            passengerEmailField.setText(user.getEmail());
        }

        // Nationality / Meal combos
        if (nationalityCombo != null) {
            nationalityCombo.getItems().addAll(
                "Kenyan","British","American","Emirati","French",
                "Dutch","South African","Indian","Singaporean","Australian","Other");
            nationalityCombo.setValue("Kenyan");
        }
        if (mealCombo != null) {
            mealCombo.getItems().addAll(
                "Standard","Vegetarian","Vegan","Halal","Kosher","Diabetic","No Meal");
            mealCombo.setValue("Standard");
        }

        if (f == null) return;

        // Seat class and pricing multiplier
        int rowNum = 1;
        String seatClass = "Economy";
        if (seat != null && !seat.isEmpty()) {
            try { rowNum = Integer.parseInt(seat.replaceAll("[^0-9]", "")); } catch (NumberFormatException ignored) {}
            seatClass = rowNum <= 3 ? "First Class" : rowNum <= 7 ? "Business" : "Economy";
        }

        double multiplier = switch (seatClass) {
            case "First Class" -> 3.0;
            case "Business"    -> 1.8;
            default            -> 1.0;
        };

        basePrice = f.getPrice() * multiplier;
        tax       = basePrice * 0.16;  // 16% VAT
        total     = basePrice + tax;

        if (summaryFlightNum != null) summaryFlightNum.setText(f.getFlightNumber());
        if (summaryRoute     != null) summaryRoute.setText(f.getOrigin() + "  →  " + f.getDestination());
        if (summaryDeparture != null) summaryDeparture.setText(f.getDeparture() + "  —  " + f.getArrival()
                                                               + "  (" + f.getDuration() + ")");
        if (summaryAircraft  != null) summaryAircraft.setText(f.getAircraftType());
        if (summarySeat      != null) summarySeat.setText(seat != null ? seat : "Not selected");
        if (summaryClass     != null) summaryClass.setText(seatClass);
        if (summaryBasePrice != null) summaryBasePrice.setText(String.format("$%.2f", basePrice));
        if (summaryTax       != null) summaryTax.setText(String.format("$%.2f", tax));
        if (summaryTotal     != null) summaryTotal.setText(String.format("$%.2f", total));
    }

    // ────────────────────────────────────────────
    //  Confirm Booking
    // ────────────────────────────────────────────

    @FXML
    public void handleConfirmBooking() {
        String name     = passengerNameField  != null ? passengerNameField.getText().trim()   : "";
        String email    = passengerEmailField != null ? passengerEmailField.getText().trim()   : "";
        String passport = passengerPassportField != null ? passengerPassportField.getText().trim() : "";

        if (name.isEmpty() || email.isEmpty() || passport.isEmpty()) {
            showAlert("Incomplete Details", "Please fill in Name, Email, and Passport Number.");
            return;
        }

        confirmBtn.setDisable(true);
        confirmBtn.setText("Processing...");

        PauseTransition pause = new PauseTransition(Duration.millis(1200));
        pause.setOnFinished(e -> {
            Flight f    = DataStore.getInstance().getSelectedFlight();
            String seat = DataStore.getInstance().getSelectedSeat();

            int rowNum = 1;
            String seatClass = "Economy";
            if (seat != null) {
                try { rowNum = Integer.parseInt(seat.replaceAll("[^0-9]", "")); } catch (NumberFormatException ignored) {}
                seatClass = rowNum <= 3 ? "First Class" : rowNum <= 7 ? "Business" : "Economy";
            }

            Booking booking;
            try {
                booking = DataStore.getInstance().addBookingFromDetails(name, total);
            } catch (RuntimeException ex) {
                confirmBtn.setDisable(false);
                confirmBtn.setText("Confirm Booking");
                showAlert("Booking Failed", ex.getMessage());
                return;
            }

            // Show confirmation banner
            if (bookingRefLabel  != null) bookingRefLabel.setText("Booking Reference: " + booking.getBookingRef());
            if (formPane         != null) {
                FadeTransition fo = new FadeTransition(Duration.millis(300), formPane);
                fo.setToValue(0);
                fo.setOnFinished(ev -> {
                    formPane.setVisible(false);
                    confirmationPane.setVisible(true);
                    FadeTransition fi = new FadeTransition(Duration.millis(500), confirmationPane);
                    fi.setFromValue(0); fi.setToValue(1); fi.play();
                });
                fo.play();
            }

            loadHistory();
        });
        pause.play();
    }

    @FXML
    public void printTicket() {
        showAlert("Print Ticket",
            "Ticket printed to PDF!\n(In the full version this would open a PDF preview.)");
    }

    @FXML
    public void newBooking() {
        DataStore.getInstance().setSelectedFlight(null);
        DataStore.getInstance().setSelectedSeat(null);
        Main.navigateTo("search");
    }

    @FXML
    public void cancelSelectedBooking() {
        Booking selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a booking to cancel.");
            return;
        }
        if ("CANCELLED".equalsIgnoreCase(selected.getStatus())) {
            showAlert("Already Cancelled", "This booking has already been cancelled.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Cancel booking " + selected.getBookingRef() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirm Cancellation");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
            return;
        }

        try {
            DataStore.getInstance().cancelBooking(selected);
            loadHistory();
            showAlert("Booking Cancelled", "Booking " + selected.getBookingRef() + " has been cancelled.");
        } catch (RuntimeException ex) {
            showAlert("Cancellation Failed", ex.getMessage());
        }
    }

    // ────────────────────────────────────────────
    //  Booking History Table
    // ────────────────────────────────────────────

    private void configureHistoryTable() {
        if (historyTable == null) return;

        colRef.setCellValueFactory(new PropertyValueFactory<>("bookingRef"));
        colFlight.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colRoute.setCellValueFactory(new PropertyValueFactory<>("route"));
        colDepart.setCellValueFactory(new PropertyValueFactory<>("departure"));
        colSeat.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        colClass.setCellValueFactory(new PropertyValueFactory<>("seatClass"));
        colCost.setCellValueFactory(new PropertyValueFactory<>("totalCost"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("bookingDate"));

        // Format cost
        colCost.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double cost, boolean empty) {
                super.updateItem(cost, empty);
                setText(empty || cost == null ? null : String.format("$%.2f", cost));
            }
        });

        // Color status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                String color = switch (status) {
                    case "CONFIRMED"  -> "#00d4aa";
                    case "CANCELLED"  -> "#ff6b6b";
                    default           -> "#f4a836";
                };
                setStyle("-fx-text-fill:" + color + "; -fx-font-weight:bold;");
            }
        });
    }

    private void loadHistory() {
        if (historyTable == null) return;
        ObservableList<Booking> all = DataStore.getInstance().getBookings();
        historyTable.setItems(all);
    }

    // ────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ---- Sidebar Navigation ----
    @FXML public void goToDashboard() { Main.navigateTo("dashboard"); }
    @FXML public void goToSearch()    { Main.navigateTo("search"); }
    @FXML public void goToBookings()  { /* already here */ }
    @FXML public void goToAdmin()     { Main.navigateTo("admin"); }
    @FXML public void handleLogout()  {
        DataStore.getInstance().setCurrentUser(null);
        Main.navigateTo("login");
    }
}
