package controllers;

import app.Main;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import models.DataStore;
import models.Flight;

import java.net.URL;
import java.util.*;

/**
 * Controller for the Seat Selection screen.
 * Dynamically generates an aircraft-style cabin layout.
 */
public class SeatSelectionController implements Initializable {

    @FXML private Label         flightInfoLabel;
    @FXML private Label         selectedSeatLabel;
    @FXML private Button        confirmSeatBtn;
    @FXML private GridPane      seatGrid;
    @FXML private Label         seatCountLabel;
    @FXML private VBox          legendBox;

    private static final int    ROWS           = 20;
    private static final String[] COLS         = {"A","B","C","","D","E","F"};
    private static final Set<String> BOOKED    = new HashSet<>(Arrays.asList(
        "1A","1B","2C","3A","3D","4F","5B","5C","6E",
        "7A","8B","9C","9D","10F","11A","12E","13B",
        "14D","15A","15F","16C","17B","18E","19A","20D"
    ));

    private Button selectedButton = null;
    private String selectedSeat   = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Flight f = DataStore.getInstance().getSelectedFlight();
        if (f != null) {
            flightInfoLabel.setText(
                f.getFlightNumber() + "  ·  " + f.getOrigin() + " → " + f.getDestination()
                + "  ·  " + f.getAircraftType());
            seatCountLabel.setText(f.getAvailableSeats() + " seats available");
        }

        selectedSeatLabel.setText("No seat selected");
        confirmSeatBtn.setDisable(true);
        buildSeatMap();
    }

    private void buildSeatMap() {
        seatGrid.getChildren().clear();
        seatGrid.setHgap(6);
        seatGrid.setVgap(6);
        seatGrid.setAlignment(Pos.CENTER);

        // Row header spacer
        seatGrid.add(new Label(""), 0, 0);
        int colIdx = 1;
        for (String col : COLS) {
            Label h = new Label(col.isEmpty() ? "" : col);
            h.getStyleClass().add("seat-header");
            h.setMinWidth(36);
            h.setAlignment(Pos.CENTER);
            seatGrid.add(h, colIdx++, 0);
        }

        for (int row = 1; row <= ROWS; row++) {
            // Row number
            Label rowLabel = new Label(String.valueOf(row));
            rowLabel.getStyleClass().add("seat-row-label");
            rowLabel.setMinWidth(28);
            rowLabel.setAlignment(Pos.CENTER_RIGHT);
            seatGrid.add(rowLabel, 0, row);

            colIdx = 1;
            for (String col : COLS) {
                if (col.isEmpty()) {
                    // Aisle spacer
                    Region spacer = new Region();
                    spacer.setMinWidth(16);
                    seatGrid.add(spacer, colIdx++, row);
                    continue;
                }
                String seatId = row + col;
                Button seatBtn = createSeatButton(seatId, row);
                seatGrid.add(seatBtn, colIdx++, row);
            }
        }
    }

    private Button createSeatButton(String seatId, int row) {
        Button btn = new Button(seatId);
        btn.setMinSize(36, 32);
        btn.setMaxSize(36, 32);

        if (BOOKED.contains(seatId)) {
            btn.getStyleClass().addAll("seat-btn", "seat-booked");
            btn.setDisable(true);
        } else if (row <= 3) {
            btn.getStyleClass().addAll("seat-btn", "seat-first");
        } else if (row <= 7) {
            btn.getStyleClass().addAll("seat-btn", "seat-business");
        } else {
            btn.getStyleClass().addAll("seat-btn", "seat-available");
        }

        btn.setOnAction(e -> handleSeatClick(btn, seatId));

        // Hover scale
        btn.setOnMouseEntered(e -> {
            if (!BOOKED.contains(seatId)) {
                ScaleTransition sc = new ScaleTransition(Duration.millis(100), btn);
                sc.setToX(1.15); sc.setToY(1.15);
                sc.play();
            }
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition sc = new ScaleTransition(Duration.millis(100), btn);
            sc.setToX(1.0); sc.setToY(1.0);
            sc.play();
        });

        return btn;
    }

    private void handleSeatClick(Button btn, String seatId) {
        // Deselect previous
        if (selectedButton != null) {
            selectedButton.getStyleClass().remove("seat-selected");
            // Re-apply original class
            int rowNum = Integer.parseInt(seatId.replaceAll("[^0-9]", ""));
            if (rowNum <= 3)      selectedButton.getStyleClass().add("seat-first");
            else if (rowNum <= 7) selectedButton.getStyleClass().add("seat-business");
            else                  selectedButton.getStyleClass().add("seat-available");
        }

        // Select new seat
        btn.getStyleClass().removeAll("seat-available", "seat-business", "seat-first");
        btn.getStyleClass().add("seat-selected");
        selectedButton = btn;
        selectedSeat   = seatId;

        int rowNum = Integer.parseInt(seatId.replaceAll("[^0-9]", ""));
        String cls = rowNum <= 3 ? "First Class" : rowNum <= 7 ? "Business" : "Economy";
        selectedSeatLabel.setText("Seat " + seatId + "  ·  " + cls);
        confirmSeatBtn.setDisable(false);

        // Pop animation
        ScaleTransition pop = new ScaleTransition(Duration.millis(150), btn);
        pop.setFromX(0.9); pop.setFromY(0.9);
        pop.setToX(1.0);   pop.setToY(1.0);
        pop.play();
    }

    @FXML
    public void confirmSeat() {
        if (selectedSeat == null) return;
        DataStore.getInstance().setSelectedSeat(selectedSeat);
        Main.navigateTo("booking");
    }

    @FXML
    public void goBack() {
        Main.navigateTo("search");
    }

    // ---- Sidebar Navigation ----
    @FXML public void goToDashboard() { Main.navigateTo("dashboard"); }
    @FXML public void goToSearch()    { Main.navigateTo("search"); }
    @FXML public void goToBookings()  { Main.navigateTo("booking"); }
    @FXML public void goToAdmin()     { Main.navigateTo("admin"); }
    @FXML public void handleLogout()  {
        DataStore.getInstance().setCurrentUser(null);
        Main.navigateTo("login");
    }
}
