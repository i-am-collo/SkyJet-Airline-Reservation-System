package controllers;

import app.Main;
import javafx.animation.*;
import javafx.collections.FXCollections;
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
import java.time.LocalDate;
import java.util.ResourceBundle;

/**
 * Controller for the Search screen and Flight Results table.
 * Populates a TableView with mock flights and handles selection.
 */
public class SearchController implements Initializable {

    // Search form
    @FXML private ComboBox<String> originCombo;
    @FXML private ComboBox<String> destinationCombo;
    @FXML private DatePicker       departureDatePicker;
    @FXML private Spinner<Integer> passengersSpinner;
    @FXML private ComboBox<String> classCombo;
    @FXML private Button           searchBtn;
    @FXML private Label            resultCountLabel;

    // Results table
    @FXML private TableView<Flight>            flightTable;
    @FXML private TableColumn<Flight, String>  colFlightNum;
    @FXML private TableColumn<Flight, String>  colAirline;
    @FXML private TableColumn<Flight, String>  colOrigin;
    @FXML private TableColumn<Flight, String>  colDestination;
    @FXML private TableColumn<Flight, String>  colDeparture;
    @FXML private TableColumn<Flight, String>  colArrival;
    @FXML private TableColumn<Flight, String>  colDuration;
    @FXML private TableColumn<Flight, Double>  colPrice;
    @FXML private TableColumn<Flight, Integer> colSeats;
    @FXML private TableColumn<Flight, String>  colStatus;

    @FXML private VBox resultsPane;
    @FXML private Label noResultsLabel;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        populateCombos();
        configureTable();
        loadAllFlights();

        departureDatePicker.setValue(LocalDate.now().plusDays(1));
        passengersSpinner.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1));

        // Animate results section
        resultsPane.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(600), resultsPane);
        ft.setToValue(1.0);
        ft.play();
    }

    private void populateCombos() {
        javafx.collections.ObservableList<String> airports =
                DataStore.getInstance().getAirports();

        originCombo.setItems(airports);
        destinationCombo.setItems(FXCollections.observableArrayList(airports));

        // Set sensible defaults
        if (airports.contains("Nairobi (NBO)")) {
            originCombo.setValue("Nairobi (NBO)");
        } else if (airports.size() > 1) {
            originCombo.setValue(airports.get(1));
        } else {
            originCombo.setValue("Any");
        }
        destinationCombo.setValue("Any");

        classCombo.getItems().addAll("Economy", "Business", "First Class");
        classCombo.setValue("Economy");
    }

    private void configureTable() {
        colFlightNum.setCellValueFactory(new PropertyValueFactory<>("flightNumber"));
        colAirline.setCellValueFactory(new PropertyValueFactory<>("airline"));
        colOrigin.setCellValueFactory(new PropertyValueFactory<>("origin"));
        colDestination.setCellValueFactory(new PropertyValueFactory<>("destination"));
        colDeparture.setCellValueFactory(new PropertyValueFactory<>("departure"));
        colArrival.setCellValueFactory(new PropertyValueFactory<>("arrival"));
        colDuration.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colPrice.setCellValueFactory(new PropertyValueFactory<>("price"));
        colSeats.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Format price column
        colPrice.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? null : String.format("$%.0f", price));
            }
        });

        // Color status column
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                String c = switch (status) {
                    case "DELAYED" -> "#f4a836";
                    case "CANCELLED" -> "#ff6b6b";
                    case "BOARDING" -> "#00b4d8";
                    case "DEPARTED" -> "#a855f7";
                    case "ARRIVED" -> "#22c55e";
                    default -> "#00d4aa";  // SCHEDULED
                };
                setStyle("-fx-text-fill: " + c + "; -fx-font-weight: bold;");
            }
        });

        // Color available seats
        colSeats.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(Integer seats, boolean empty) {
                super.updateItem(seats, empty);
                if (empty || seats == null) { setText(null); setStyle(""); return; }
                setText(String.valueOf(seats));
                setStyle(seats <= 10
                    ? "-fx-text-fill: #ff6b6b;"
                    : "-fx-text-fill: #00d4aa;");
            }
        });

        // Row double-click to book
        flightTable.setRowFactory(tv -> {
            TableRow<Flight> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    selectFlight(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadAllFlights() {
        ObservableList<Flight> all = DataStore.getInstance().getFlights();
        flightTable.setItems(all);
        updateResultCount(all.size());
    }

    @FXML
    public void handleSearch() {
        String origin      = originCombo.getValue();
        String destination = destinationCombo.getValue();

        searchBtn.setDisable(true);
        searchBtn.setText("Searching...");

        PauseTransition pause = new PauseTransition(Duration.millis(600));
        pause.setOnFinished(e -> {
            ObservableList<Flight> results =
                DataStore.getInstance().searchFlights(origin, destination);
            flightTable.setItems(results);
            updateResultCount(results.size());
            noResultsLabel.setVisible(results.isEmpty());

            searchBtn.setDisable(false);
            searchBtn.setText("Search Flights");

            // Bounce animation on the table
            ScaleTransition bounce = new ScaleTransition(Duration.millis(200), flightTable);
            bounce.setFromX(0.98); bounce.setFromY(0.98);
            bounce.setToX(1.0);   bounce.setToY(1.0);
            bounce.play();
        });
        pause.play();
    }

    @FXML
    public void bookSelected() {
        Flight f = flightTable.getSelectionModel().getSelectedItem();
        if (f != null) {
            selectFlight(f);
        } else {
            showAlert("No Selection", "Please select a flight from the table first.");
        }
    }

    private void selectFlight(Flight f) {
        DataStore.getInstance().setSelectedFlight(f);
        Main.navigateTo("seats");
    }

    private void updateResultCount(int count) {
        resultCountLabel.setText(count + " flight" + (count == 1 ? "" : "s") + " found");
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // ---- Sidebar Navigation ----
    @FXML public void goToDashboard() { Main.navigateTo("dashboard"); }
    @FXML public void goToSearch()    { /* already here */ }
    @FXML public void goToBookings()  { Main.navigateTo("booking"); }
    @FXML public void goToAdmin()     { Main.navigateTo("admin"); }
    @FXML public void handleLogout()  {
        DataStore.getInstance().setCurrentUser(null);
        Main.navigateTo("login");
    }
}
