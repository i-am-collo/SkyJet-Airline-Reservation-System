package models;

import javafx.beans.property.*;

/**
 * Flight model representing a single flight entry.
 * Uses JavaFX properties so TableView binds automatically.
 */
public class Flight {

    private long id;
    private final StringProperty  flightNumber;
    private final StringProperty  airline;
    private final StringProperty  origin;
    private final StringProperty  destination;
    private final StringProperty  departure;
    private final StringProperty  arrival;
    private final StringProperty  duration;
    private final DoubleProperty  price;
    private final IntegerProperty availableSeats;
    private final StringProperty  aircraftType;
    private final StringProperty  status;

    public Flight(String flightNumber, String airline, String origin,
                  String destination, String departure, String arrival,
                  String duration, double price, int availableSeats,
                  String aircraftType, String status) {
        this(0, flightNumber, airline, origin, destination, departure, arrival,
                duration, price, availableSeats, aircraftType, status);
    }

    public Flight(long id, String flightNumber, String airline, String origin,
                  String destination, String departure, String arrival,
                  String duration, double price, int availableSeats,
                  String aircraftType, String status) {
        this.id             = id;
        this.flightNumber   = new SimpleStringProperty(flightNumber);
        this.airline        = new SimpleStringProperty(airline);
        this.origin         = new SimpleStringProperty(origin);
        this.destination    = new SimpleStringProperty(destination);
        this.departure      = new SimpleStringProperty(departure);
        this.arrival        = new SimpleStringProperty(arrival);
        this.duration       = new SimpleStringProperty(duration);
        this.price          = new SimpleDoubleProperty(price);
        this.availableSeats = new SimpleIntegerProperty(availableSeats);
        this.aircraftType   = new SimpleStringProperty(aircraftType);
        this.status         = new SimpleStringProperty(status);
    }

    // ---- Getters ----
    public long   getId()             { return id; }
    public String getFlightNumber()   { return flightNumber.get(); }
    public String getAirline()        { return airline.get(); }
    public String getOrigin()         { return origin.get(); }
    public String getDestination()    { return destination.get(); }
    public String getDeparture()      { return departure.get(); }
    public String getArrival()        { return arrival.get(); }
    public String getDuration()       { return duration.get(); }
    public double getPrice()          { return price.get(); }
    public int    getAvailableSeats() { return availableSeats.get(); }
    public String getAircraftType()   { return aircraftType.get(); }
    public String getStatus()         { return status.get(); }

    // ---- Property accessors (for TableView binding) ----
    public StringProperty  flightNumberProperty()   { return flightNumber; }
    public StringProperty  airlineProperty()        { return airline; }
    public StringProperty  originProperty()         { return origin; }
    public StringProperty  destinationProperty()    { return destination; }
    public StringProperty  departureProperty()      { return departure; }
    public StringProperty  arrivalProperty()        { return arrival; }
    public StringProperty  durationProperty()       { return duration; }
    public DoubleProperty  priceProperty()          { return price; }
    public IntegerProperty availableSeatsProperty() { return availableSeats; }
    public StringProperty  aircraftTypeProperty()   { return aircraftType; }
    public StringProperty  statusProperty()         { return status; }

    // ---- Setters ----
    public void setId(long v)              { id = v; }
    public void setFlightNumber(String v)   { flightNumber.set(v); }
    public void setAirline(String v)        { airline.set(v); }
    public void setOrigin(String v)         { origin.set(v); }
    public void setDestination(String v)    { destination.set(v); }
    public void setDeparture(String v)      { departure.set(v); }
    public void setArrival(String v)        { arrival.set(v); }
    public void setDuration(String v)       { duration.set(v); }
    public void setPrice(double v)          { price.set(v); }
    public void setAvailableSeats(int v)    { availableSeats.set(v); }
    public void setAircraftType(String v)   { aircraftType.set(v); }
    public void setStatus(String v)         { status.set(v); }

    @Override
    public String toString() {
        return flightNumber.get() + " | " + origin.get() + " → " + destination.get();
    }
}
