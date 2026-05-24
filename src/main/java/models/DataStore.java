package models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import services.ApiClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Backend-backed application state for the JavaFX client.
 */
public class DataStore {

    private static DataStore instance;

    private ObservableList<Flight> flights;
    private ObservableList<Booking> bookings;
    private List<User> users;
    private List<LoginAudit> loginAudits;
    private final ApiClient apiClient;
    private User currentUser;
    private Flight selectedFlight;
    private String selectedSeat;

    private DataStore() {
        apiClient = ApiClient.getInstance();
        flights = FXCollections.observableArrayList();
        bookings = FXCollections.observableArrayList();
        users = new ArrayList<>();
        loginAudits = new ArrayList<>();
    }

    public static DataStore getInstance() {
        if (instance == null)
            instance = new DataStore();
        return instance;
    }

    // ───────────────────────────────────────────
    // Initialise mock data
    // ───────────────────────────────────────────

    private void initUsers() {
        users = new ArrayList<>();
        // Store plain passwords for demo (in production, use pre-hashed values)
        users.add(new User("U001", "James Carter", "james@skyjet.com", "password123", "USER"));
        users.add(new User("U002", "Admin User", "admin@skyjet.com", "admin123", "ADMIN"));
    }

    private void initFlights() {
        flights = FXCollections.observableArrayList(
                new Flight("SJ-101", "SkyJet", "Nairobi (NBO)", "London (LHR)",
                        "08:00", "20:15", "9h 15m", 850.00, 42, "Boeing 787", "ON TIME"),
                new Flight("SJ-205", "SkyJet", "Nairobi (NBO)", "Dubai (DXB)",
                        "09:45", "15:30", "5h 45m", 420.00, 18, "Airbus A330", "ON TIME"),
                new Flight("SJ-312", "SkyJet", "Nairobi (NBO)", "Amsterdam (AMS)",
                        "11:30", "22:45", "10h 15m", 790.00, 35, "Boeing 777", "DELAYED"),
                new Flight("SJ-418", "SkyJet", "Nairobi (NBO)", "New York (JFK)",
                        "14:00", "06:30", "15h 30m", 1250.00, 8, "Boeing 787", "ON TIME"),
                new Flight("SJ-522", "SkyJet", "Nairobi (NBO)", "Paris (CDG)",
                        "16:15", "04:30", "11h 15m", 880.00, 27, "Airbus A350", "ON TIME"),
                new Flight("SJ-630", "SkyJet", "Nairobi (NBO)", "Johannesburg (JNB)",
                        "06:00", "09:45", "3h 45m", 210.00, 55, "Boeing 737", "ON TIME"),
                new Flight("SJ-744", "SkyJet", "Nairobi (NBO)", "Mumbai (BOM)",
                        "23:00", "06:30", "6h 30m", 380.00, 31, "Airbus A320", "ON TIME"),
                new Flight("SJ-856", "SkyJet", "Nairobi (NBO)", "Singapore (SIN)",
                        "02:00", "18:15", "11h 15m", 950.00, 12, "Boeing 787", "ON TIME"),
                new Flight("SJ-960", "SkyJet", "Nairobi (NBO)", "Cairo (CAI)",
                        "07:30", "12:00", "4h 30m", 310.00, 44, "Airbus A320", "ON TIME"),
                new Flight("SJ-1072", "SkyJet", "Nairobi (NBO)", "Sydney (SYD)",
                        "21:00", "23:30", "21h 30m", 1650.00, 5, "Boeing 777", "ON TIME"));
    }

    private void initBookings() {
        bookings = FXCollections.observableArrayList(
                new Booking("BK-9281", "James Carter", "SJ-101",
                        "Nairobi → London", "08:00 — 20:15", "14A",
                        "Economy", 850.00, "CONFIRMED", "2024-06-01"),
                new Booking("BK-8472", "James Carter", "SJ-205",
                        "Nairobi → Dubai", "09:45 — 15:30", "22C",
                        "Business", 1240.00, "CONFIRMED", "2024-05-18"),
                new Booking("BK-7361", "James Carter", "SJ-630",
                        "Nairobi → Johannesburg", "06:00 — 09:45", "5F",
                        "Economy", 210.00, "CANCELLED", "2024-04-22"),
                new Booking("BK-6250", "James Carter", "SJ-744",
                        "Nairobi → Mumbai", "23:00 — 06:30", "31D",
                        "Economy", 380.00, "CONFIRMED", "2024-03-10"));
    }

    // ───────────────────────────────────────────
    // Public API
    // ───────────────────────────────────────────

    public User login(String email, String password) {
        try {
            ApiClient.AuthResult result = apiClient.login(email, password);
            SessionManager.getInstance().createSession(result.user(), result.token(), result.expiresIn());
            currentUser = result.user();
            return currentUser;
        } catch (Exception ex) {
            auditLogin(null, email, false, ex.getMessage());
            return null;
        }
    }

    public boolean registerUser(String fullName, String email, String password) {
        try {
            apiClient.register(fullName, email, password);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public ObservableList<Flight> searchFlights(String origin, String destination) {
        try {
            flights = apiClient.getFlights(origin, destination);
            return flights;
        } catch (Exception ex) {
            return FXCollections.observableArrayList();
        }
    }

    public void addBooking(Booking b) {
        try {
            Flight flight = getSelectedFlight();
            Booking saved = apiClient.createBooking(
                    flight != null ? flight.getId() : 0,
                    b.getSeatNumber(),
                    b.getPassengerName(),
                    b.getTotalCost());
            bookings.add(saved);
            if (currentUser != null)
                currentUser.setTotalBookings(currentUser.getTotalBookings() + 1);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public Booking addBookingFromDetails(String passengerName, double totalCost) {
        try {
            Flight flight = getSelectedFlight();
            Booking saved = apiClient.createBooking(
                    flight != null ? flight.getId() : 0,
                    selectedSeat,
                    passengerName,
                    totalCost);
            bookings.add(saved);
            if (currentUser != null)
                currentUser.setTotalBookings(currentUser.getTotalBookings() + 1);
            return saved;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public void refreshBookings() {
        try {
            bookings = apiClient.getBookings();
            if (currentUser != null)
                currentUser.setTotalBookings(bookings.size());
        } catch (Exception ex) {
            bookings = FXCollections.observableArrayList();
        }
    }

    public void refreshFlights() {
        try {
            flights = apiClient.getFlights();
        } catch (Exception ex) {
            flights = FXCollections.observableArrayList();
        }
    }

    public void updateFlight(Flight f) {
        try {
            Flight saved = apiClient.updateFlight(f);
            f.setId(saved.getId());
            f.setFlightNumber(saved.getFlightNumber());
            f.setAirline(saved.getAirline());
            f.setOrigin(saved.getOrigin());
            f.setDestination(saved.getDestination());
            f.setDeparture(saved.getDeparture());
            f.setArrival(saved.getArrival());
            f.setDuration(saved.getDuration());
            f.setPrice(saved.getPrice());
            f.setAvailableSeats(saved.getAvailableSeats());
            f.setAircraftType(saved.getAircraftType());
            f.setStatus(saved.getStatus());
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public void addFlight(Flight f) {
        try {
            flights.add(apiClient.createFlight(f));
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public void removeFlight(Flight f) {
        try {
            apiClient.deleteFlight(f);
            flights.remove(f);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    // ---- State getters/setters ----
    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User u) {
        currentUser = u;
        if (u == null) {
            SessionManager.getInstance().invalidateSession();
            bookings.clear();
        }
    }

    public Flight getSelectedFlight() {
        return selectedFlight;
    }

    public void setSelectedFlight(Flight f) {
        selectedFlight = f;
    }

    public String getSelectedSeat() {
        return selectedSeat;
    }

    public void setSelectedSeat(String s) {
        selectedSeat = s;
    }

    public ObservableList<Flight> getFlights() {
        refreshFlights();
        return flights;
    }

    public ObservableList<Booking> getBookings() {
        if (SessionManager.getInstance().isSessionValid()) {
            refreshBookings();
        }
        return bookings;
    }

    // ───────────────────────────────────────────
    // Audit & Security
    // ───────────────────────────────────────────

    /**
     * Record a login attempt in the audit log
     */
    private void auditLogin(String userId, String email, boolean success, String reason) {
        LoginAudit audit = new LoginAudit(userId != null ? userId : "UNKNOWN", email, success, reason);
        loginAudits.add(audit);
        // Optional: print to console for debugging
        System.out.println("[AUDIT] " + audit);
    }

    /**
     * Get all login audit logs
     */
    public List<LoginAudit> getLoginAudits() {
        return new ArrayList<>(loginAudits);
    }

    /**
     * Clear audit logs (useful for testing)
     */
    public void clearAuditLogs() {
        loginAudits.clear();
    }
}
