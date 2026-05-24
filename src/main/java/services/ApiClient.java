package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import models.Booking;
import models.Flight;
import models.SessionManager;
import models.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final ApiClient INSTANCE = new ApiClient();

    private final HttpClient httpClient;
    private final ObjectMapper mapper;
    private final String baseUrl;

    private ApiClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        this.mapper = new ObjectMapper();
        this.baseUrl = System.getProperty("skyjet.api.baseUrl", DEFAULT_BASE_URL);
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public AuthResult login(String email, String password) throws IOException, InterruptedException {
        Map<String, Object> body = new HashMap<>();
        body.put("email", email);
        body.put("password", password);
        JsonNode json = send("/api/auth/login", "POST", body, false);
        return authResult(json);
    }

    public AuthResult register(String name, String email, String password) throws IOException, InterruptedException {
        Map<String, Object> body = new HashMap<>();
        body.put("name", name);
        body.put("email", email);
        body.put("password", password);
        JsonNode json = send("/api/auth/register", "POST", body, false);
        return authResult(json);
    }

    public ObservableList<Flight> getFlights() throws IOException, InterruptedException {
        return getFlights(null, null);
    }

    public ObservableList<Flight> getFlights(String origin, String destination) throws IOException, InterruptedException {
        StringBuilder path = new StringBuilder("/api/flights");
        boolean hasQuery = false;
        if (hasFilter(origin)) {
            path.append("?origin=").append(encode(origin));
            hasQuery = true;
        }
        if (hasFilter(destination)) {
            path.append(hasQuery ? "&" : "?")
                    .append("destination=").append(encode(destination));
        }

        JsonNode json = send(path.toString(), "GET", null, false);
        ObservableList<Flight> flights = FXCollections.observableArrayList();
        for (JsonNode item : json) {
            flights.add(toFlight(item));
        }
        return flights;
    }

    public Flight createFlight(Flight flight) throws IOException, InterruptedException {
        return toFlight(send("/api/admin/flights", "POST", flightBody(flight), true));
    }

    public Flight updateFlight(Flight flight) throws IOException, InterruptedException {
        return toFlight(send("/api/admin/flights/" + flight.getId(), "PUT", flightBody(flight), true));
    }

    public void deleteFlight(Flight flight) throws IOException, InterruptedException {
        send("/api/admin/flights/" + flight.getId(), "DELETE", null, true);
    }

    public ObservableList<Booking> getBookings() throws IOException, InterruptedException {
        JsonNode json = send("/api/bookings", "GET", null, true);
        ObservableList<Booking> bookings = FXCollections.observableArrayList();
        for (JsonNode item : json) {
            bookings.add(toBooking(item));
        }
        return bookings;
    }

    public Booking createBooking(long flightId, String seatNumber, String passengerName, double totalCost)
            throws IOException, InterruptedException {
        Map<String, Object> body = new HashMap<>();
        body.put("flightId", flightId);
        body.put("seatNumber", seatNumber);
        body.put("passengerName", passengerName);
        body.put("totalCost", BigDecimal.valueOf(totalCost));
        return toBooking(send("/api/bookings", "POST", body, true));
    }

    private JsonNode send(String path, String method, Object body, boolean authenticated)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");

        if (authenticated) {
            String token = SessionManager.getInstance().getSessionToken();
            if (token != null) {
                builder.header("Authorization", "Bearer " + token);
            }
        }

        if (body == null) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
        }

        HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException(errorMessage(response));
        }
        if (response.body() == null || response.body().isBlank()) {
            return mapper.createObjectNode();
        }
        return mapper.readTree(response.body());
    }

    private AuthResult authResult(JsonNode json) {
        User user = new User(
                text(json, "userId"),
                text(json, "name"),
                text(json, "email"),
                "",
                text(json, "role"));
        return new AuthResult(user, text(json, "token"), json.path("expiresIn").asLong(3600));
    }

    private Flight toFlight(JsonNode item) {
        return new Flight(
                item.path("id").asLong(),
                text(item, "flightNumber"),
                text(item, "airline"),
                text(item, "origin"),
                text(item, "destination"),
                text(item, "departureTime"),
                text(item, "arrivalTime"),
                text(item, "duration"),
                item.path("price").asDouble(),
                item.path("availableSeats").asInt(),
                text(item, "aircraft"),
                text(item, "status"));
    }

    private Booking toBooking(JsonNode item) {
        String seatClass = seatClass(text(item, "seatNumber"));
        String bookingDate = text(item, "bookingDate");
        if (bookingDate.length() >= 10) {
            bookingDate = bookingDate.substring(0, 10);
        }

        return new Booking(
                item.path("id").asLong(),
                text(item, "bookingId"),
                text(item, "passengerName"),
                text(item, "flightNumber"),
                text(item, "route"),
                text(item, "schedule"),
                text(item, "seatNumber"),
                seatClass,
                item.path("totalCost").asDouble(),
                text(item, "status"),
                bookingDate);
    }

    private Map<String, Object> flightBody(Flight flight) {
        Map<String, Object> body = new HashMap<>();
        body.put("flightNumber", flight.getFlightNumber());
        body.put("airline", flight.getAirline());
        body.put("aircraft", flight.getAircraftType());
        body.put("origin", flight.getOrigin());
        body.put("destination", flight.getDestination());
        body.put("departureTime", flight.getDeparture());
        body.put("arrivalTime", flight.getArrival());
        body.put("duration", flight.getDuration());
        body.put("price", BigDecimal.valueOf(flight.getPrice()));
        body.put("availableSeats", flight.getAvailableSeats());
        body.put("status", flight.getStatus());
        return body;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? "" : value.asText();
    }

    private boolean hasFilter(String value) {
        return value != null && !value.isBlank() && !"Any".equalsIgnoreCase(value);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String seatClass(String seatNumber) {
        if (seatNumber == null || seatNumber.isBlank()) {
            return "Economy";
        }
        try {
            int row = Integer.parseInt(seatNumber.replaceAll("[^0-9]", ""));
            if (row <= 3) {
                return "First Class";
            }
            if (row <= 7) {
                return "Business";
            }
        } catch (NumberFormatException ignored) {
        }
        return "Economy";
    }

    private String errorMessage(HttpResponse<String> response) {
        try {
            JsonNode json = mapper.readTree(response.body());
            if (json.hasNonNull("message")) {
                return json.get("message").asText();
            }
        } catch (Exception ignored) {
        }
        return "API request failed with status " + response.statusCode();
    }

    public record AuthResult(User user, String token, long expiresIn) {
    }
}
