package com.skyjet.backend.service;

import com.skyjet.backend.dto.FlightDTO;
import com.skyjet.backend.entity.Flight;
import com.skyjet.backend.entity.enums.FlightStatus;
import com.skyjet.backend.exception.ApiException;
import com.skyjet.backend.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;
    private final FlightSeatRepository flightSeatRepository;
    private final AirlineRepository airlineRepository;
    private final AircraftRepository aircraftRepository;
    private final AirportRepository airportRepository;
    private final AuditService auditService;

    public FlightService(FlightRepository flightRepository,
                         FlightSeatRepository flightSeatRepository,
                         AirlineRepository airlineRepository,
                         AircraftRepository aircraftRepository,
                         AirportRepository airportRepository,
                         AuditService auditService) {
        this.flightRepository = flightRepository;
        this.flightSeatRepository = flightSeatRepository;
        this.airlineRepository = airlineRepository;
        this.aircraftRepository = aircraftRepository;
        this.airportRepository = airportRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<FlightDTO> findFlights(String origin, String destination) {
        List<Flight> flights;
        if (hasText(origin) || hasText(destination)) {
            flights = flightRepository.searchByOriginAndDestination(
                    valueOrEmpty(origin), valueOrEmpty(destination));
        } else {
            flights = flightRepository.findAllActive();
        }
        return flights.stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public FlightDTO getFlight(Long id) {
        Flight flight = flightRepository.findByIdWithDetails(id);
        if (flight == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Flight not found");
        }
        long available = flightSeatRepository.countAvailableSeats(id);
        java.math.BigDecimal minPrice = flightSeatRepository.findMinPriceForFlight(id);
        return FlightDTO.fromEntity(flight, available, minPrice);
    }

    @Transactional
    public FlightDTO createFlight(FlightDTO request, String adminEmail) {
        Flight flight = Flight.builder()
                .airline(airlineRepository.findById(request.getAirlineId())
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Airline not found")))
                .aircraft(aircraftRepository.findById(request.getAircraftId())
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Aircraft not found")))
                .departureAirport(airportRepository.findById(request.getDepartureAirportId())
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Departure airport not found")))
                .arrivalAirport(airportRepository.findById(request.getArrivalAirportId())
                        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Arrival airport not found")))
                .departureTime(request.getDepartureDateTime())
                .arrivalTime(request.getArrivalDateTime())
                .status(request.getStatus() != null ?
                        FlightStatus.valueOf(request.getStatus()) : FlightStatus.SCHEDULED)
                .build();

        Flight saved = flightRepository.save(flight);

        auditService.logAction(adminEmail, "CREATE", "FLIGHT", saved.getFlightId(),
                "Created flight " + saved.getFlightId());

        return toDTO(saved);
    }

    /**
     * Overload for backward compatibility.
     */
    @Transactional
    public FlightDTO createFlight(FlightDTO request) {
        return createFlight(request, null);
    }

    @Transactional
    public FlightDTO updateFlight(Long id, FlightDTO request, String adminEmail) {
        Flight flight = findEntity(id);

        if (request.getAirlineId() != null) {
            flight.setAirline(airlineRepository.findById(request.getAirlineId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Airline not found")));
        }
        if (request.getAircraftId() != null) {
            flight.setAircraft(aircraftRepository.findById(request.getAircraftId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Aircraft not found")));
        }
        if (request.getDepartureAirportId() != null) {
            flight.setDepartureAirport(airportRepository.findById(request.getDepartureAirportId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Departure airport not found")));
        }
        if (request.getArrivalAirportId() != null) {
            flight.setArrivalAirport(airportRepository.findById(request.getArrivalAirportId())
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Arrival airport not found")));
        }
        if (request.getDepartureDateTime() != null) {
            flight.setDepartureTime(request.getDepartureDateTime());
        }
        if (request.getArrivalDateTime() != null) {
            flight.setArrivalTime(request.getArrivalDateTime());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            flight.setStatus(FlightStatus.valueOf(request.getStatus()));
        }

        Flight saved = flightRepository.save(flight);

        auditService.logAction(adminEmail, "UPDATE", "FLIGHT", saved.getFlightId(),
                "Updated flight " + saved.getFlightId());

        return toDTO(saved);
    }

    /**
     * Overload for backward compatibility.
     */
    @Transactional
    public FlightDTO updateFlight(Long id, FlightDTO request) {
        return updateFlight(id, request, null);
    }

    @Transactional
    public void deleteFlight(Long id, String adminEmail) {
        Flight flight = findEntity(id);

        auditService.logAction(adminEmail, "DELETE", "FLIGHT", id,
                "Deleted flight " + id);

        flightRepository.delete(flight);
    }

    /**
     * Overload for backward compatibility.
     */
    @Transactional
    public void deleteFlight(Long id) {
        deleteFlight(id, null);
    }

    public Flight findEntity(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Flight not found"));
    }

    private FlightDTO toDTO(Flight flight) {
        long available = flightSeatRepository.countAvailableSeats(flight.getFlightId());
        java.math.BigDecimal minPrice = flightSeatRepository.findMinPriceForFlight(flight.getFlightId());
        return FlightDTO.fromEntity(flight, available, minPrice);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank() && !"Any".equalsIgnoreCase(value);
    }

    private String valueOrEmpty(String value) {
        return hasText(value) ? value : "";
    }
}
