package models;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import services.ApiClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    private String lastLoginError;

    private DataStore() {
        apiClient = ApiClient.getInstance();
        flights = FXCollections.observableArrayList();
        bookings = FXCollections.observableArrayList();
        users = new ArrayList<>();
        loginAudits = new ArrayList<>();
        initUsers();
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
        // No demo users - users must register or authenticate via backend
    }

    // ───────────────────────────────────────────
    // Public API
    // ───────────────────────────────────────────

    public User login(String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        lastLoginError = null;

        try {
            ApiClient.AuthResult result = apiClient.login(normalizedEmail, password);
            SessionManager.getInstance().createSession(result.user(), result.token(), result.expiresIn());
            currentUser = result.user();
            return currentUser;
        } catch (Exception ex) {
            lastLoginError = ex.getMessage();
            auditLogin(null, normalizedEmail, false, lastLoginError);
            return null;
        }
    }

    public String getLastLoginError() {
        return lastLoginError;
    }

    /**
     * Registers a new user via the backend API.
     * @return null on success, or an error message string on failure.
     */
    public String registerUser(String fullName, String email, String password) {
        try {
            apiClient.register(fullName, email, password);
            return null; // success
        } catch (Exception ex) {
            String msg = ex.getMessage();
            if (msg == null || msg.isBlank()) {
                return "Registration failed. Please try again.";
            }
            return msg;
        }
    }

    /**
     * Fetches airports from the backend for dropdown population.
     * Falls back to a default list if the backend is unreachable.
     */
    public ObservableList<String> getAirports() {
        try {
            return apiClient.getAirports();
        } catch (Exception ex) {
            // Fallback to a default list if backend is unavailable
            return FXCollections.observableArrayList(
                    "Any", "Nairobi (NBO)", "London (LHR)", "Dubai (DXB)",
                    "New York (JFK)", "Paris (CDG)", "Johannesburg (JNB)");
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

    public boolean cancelBooking(Booking booking) {
        try {
            Booking cancelled = apiClient.cancelBooking(booking);
            booking.setStatus(cancelled.getStatus());
            refreshBookings();
            refreshFlights();
            return true;
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

    public ObservableList<Booking> getAdminBookings() {
        try {
            return apiClient.getAdminBookings();
        } catch (Exception ex) {
            return FXCollections.observableArrayList();
        }
    }

    public ObservableList<LoginAudit> getBackendLoginAudits() {
        try {
            return apiClient.getLoginAudits();
        } catch (Exception ex) {
            return FXCollections.observableArrayList(getLoginAudits());
        }
    }

    public Set<String> getBookedSeatsForSelectedFlight() {
        Flight flight = getSelectedFlight();
        if (flight == null || flight.getId() <= 0) {
            return new HashSet<>();
        }
        try {
            return apiClient.getBookedSeats(flight.getId());
        } catch (Exception ex) {
            return new HashSet<>();
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
