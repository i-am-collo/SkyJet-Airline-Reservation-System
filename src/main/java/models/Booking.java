package models;

import javafx.beans.property.*;

/**
 * Booking model representing a confirmed or pending ticket.
 * Uses JavaFX properties for TableView binding.
 */
public class Booking {

    private long id;
    private final StringProperty bookingRef;
    private final StringProperty passengerName;
    private final StringProperty flightNumber;
    private final StringProperty route;
    private final StringProperty departure;
    private final StringProperty seatNumber;
    private final StringProperty seatClass;
    private final DoubleProperty totalCost;
    private final StringProperty status;   // CONFIRMED, PENDING, CANCELLED
    private final StringProperty bookingDate;

    public Booking(String bookingRef, String passengerName, String flightNumber,
                   String route, String departure, String seatNumber,
                   String seatClass, double totalCost, String status,
                   String bookingDate) {
        this(0, bookingRef, passengerName, flightNumber, route, departure,
                seatNumber, seatClass, totalCost, status, bookingDate);
    }

    public Booking(long id, String bookingRef, String passengerName, String flightNumber,
                   String route, String departure, String seatNumber,
                   String seatClass, double totalCost, String status,
                   String bookingDate) {
        this.id            = id;
        this.bookingRef    = new SimpleStringProperty(bookingRef);
        this.passengerName = new SimpleStringProperty(passengerName);
        this.flightNumber  = new SimpleStringProperty(flightNumber);
        this.route         = new SimpleStringProperty(route);
        this.departure     = new SimpleStringProperty(departure);
        this.seatNumber    = new SimpleStringProperty(seatNumber);
        this.seatClass     = new SimpleStringProperty(seatClass);
        this.totalCost     = new SimpleDoubleProperty(totalCost);
        this.status        = new SimpleStringProperty(status);
        this.bookingDate   = new SimpleStringProperty(bookingDate);
    }

    // ---- Getters ----
    public long   getId()            { return id; }
    public String getBookingRef()    { return bookingRef.get(); }
    public String getPassengerName() { return passengerName.get(); }
    public String getFlightNumber()  { return flightNumber.get(); }
    public String getRoute()         { return route.get(); }
    public String getDeparture()     { return departure.get(); }
    public String getSeatNumber()    { return seatNumber.get(); }
    public String getSeatClass()     { return seatClass.get(); }
    public double getTotalCost()     { return totalCost.get(); }
    public String getStatus()        { return status.get(); }
    public String getBookingDate()   { return bookingDate.get(); }

    // ---- Property accessors ----
    public StringProperty bookingRefProperty()    { return bookingRef; }
    public StringProperty passengerNameProperty() { return passengerName; }
    public StringProperty flightNumberProperty()  { return flightNumber; }
    public StringProperty routeProperty()         { return route; }
    public StringProperty departureProperty()     { return departure; }
    public StringProperty seatNumberProperty()    { return seatNumber; }
    public StringProperty seatClassProperty()     { return seatClass; }
    public DoubleProperty totalCostProperty()     { return totalCost; }
    public StringProperty statusProperty()        { return status; }
    public StringProperty bookingDateProperty()   { return bookingDate; }

    // ---- Setters ----
    public void setId(long v)          { id = v; }
    public void setStatus(String v)      { status.set(v); }
    public void setTotalCost(double v)   { totalCost.set(v); }
}
