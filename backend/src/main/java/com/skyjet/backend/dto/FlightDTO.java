package com.skyjet.backend.dto;

import com.skyjet.backend.entity.Flight;
import com.skyjet.backend.entity.FlightSeat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * FlightDTO - Preserves the same JSON shape as the old API for frontend compatibility.
 * Internally constructs fields from the normalized entity relationships.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightDTO {
    private Long id;

    private String flightNumber;

    @NotBlank
    private String airline;

    private String aircraft;

    @NotBlank
    private String origin;

    @NotBlank
    private String destination;

    @NotBlank
    private String departureTime;

    private String arrivalTime;
    private String duration;

    private BigDecimal price;

    private Integer availableSeats;

    private String status;

    // IDs for create/update operations (admin)
    private Long airlineId;
    private Long aircraftId;
    private Long departureAirportId;
    private Long arrivalAirportId;
    private LocalDateTime departureDateTime;
    private LocalDateTime arrivalDateTime;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Build a FlightDTO from a Flight entity (with eager-loaded associations).
     * Computes composite fields for frontend compatibility.
     */
    public static FlightDTO fromEntity(Flight flight, long availableSeats) {
        String airlineName = flight.getAirline() != null ? flight.getAirline().getName() : "";
        String airlineIata = flight.getAirline() != null ? flight.getAirline().getIataCode() : "";
        String aircraftModel = flight.getAircraft() != null ? flight.getAircraft().getModel() : "";

        String depAirportName = "";
        String depIata = "";
        if (flight.getDepartureAirport() != null) {
            depAirportName = flight.getDepartureAirport().getCity();
            depIata = flight.getDepartureAirport().getIataCode();
        }

        String arrAirportName = "";
        String arrIata = "";
        if (flight.getArrivalAirport() != null) {
            arrAirportName = flight.getArrivalAirport().getCity();
            arrIata = flight.getArrivalAirport().getIataCode();
        }

        // Compute duration from departure and arrival times
        String durationStr = "";
        if (flight.getDepartureTime() != null && flight.getArrivalTime() != null) {
            Duration dur = Duration.between(flight.getDepartureTime(), flight.getArrivalTime());
            if (dur.isNegative()) {
                dur = dur.plusDays(1);  // handles overnight flights
            }
            long hours = dur.toHours();
            long minutes = dur.toMinutesPart();
            durationStr = hours + "h " + String.format("%02d", minutes) + "m";
        }

        return FlightDTO.builder()
                .id(flight.getFlightId())
                .flightNumber(airlineIata + "-" + flight.getFlightId())
                .airline(airlineName)
                .aircraft(aircraftModel)
                .origin(depAirportName + " (" + depIata + ")")
                .destination(arrAirportName + " (" + arrIata + ")")
                .departureTime(flight.getDepartureTime() != null ? flight.getDepartureTime().format(TIME_FMT) : "")
                .arrivalTime(flight.getArrivalTime() != null ? flight.getArrivalTime().format(TIME_FMT) : "")
                .duration(durationStr)
                .price(BigDecimal.ZERO)  // Price is on tickets, not flights; will be set elsewhere if needed
                .availableSeats((int) availableSeats)
                .status(flight.getStatus() != null ? flight.getStatus().name() : "SCHEDULED")
                .build();
    }

    /**
     * Convenience overload when available seats are not yet computed.
     */
    public static FlightDTO fromEntity(Flight flight) {
        return fromEntity(flight, 0, null);
    }

    /**
     * Overload with price from cheapest available seat.
     */
    public static FlightDTO fromEntity(Flight flight, long availableSeats, BigDecimal minPrice) {
        FlightDTO dto = fromEntity(flight, availableSeats);
        dto.setPrice(minPrice != null ? minPrice : BigDecimal.ZERO);
        return dto;
    }
}
