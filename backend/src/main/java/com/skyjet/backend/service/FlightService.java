package com.skyjet.backend.service;

import com.skyjet.backend.dto.FlightDTO;
import com.skyjet.backend.entity.Flight;
import com.skyjet.backend.exception.ApiException;
import com.skyjet.backend.repository.FlightRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FlightService {

    private final FlightRepository flightRepository;

    public FlightService(FlightRepository flightRepository) {
        this.flightRepository = flightRepository;
    }

    @Transactional(readOnly = true)
    public List<FlightDTO> findFlights(String origin, String destination) {
        List<Flight> flights;
        if (hasText(origin) || hasText(destination)) {
            flights = flightRepository.searchByOriginAndDestination(valueOrEmpty(origin), valueOrEmpty(destination));
        } else {
            flights = flightRepository.findAllActive();
        }
        return flights.stream().map(FlightDTO::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public FlightDTO getFlight(Long id) {
        return FlightDTO.fromEntity(findEntity(id));
    }

    @Transactional
    public FlightDTO createFlight(FlightDTO request) {
        flightRepository.findByFlightNumber(request.getFlightNumber()).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "Flight number already exists");
        });
        return FlightDTO.fromEntity(flightRepository.save(request.toEntity()));
    }

    @Transactional
    public FlightDTO updateFlight(Long id, FlightDTO request) {
        Flight flight = findEntity(id);
        flight.setFlightNumber(request.getFlightNumber());
        flight.setAirline(request.getAirline());
        flight.setAircraft(request.getAircraft());
        flight.setOrigin(request.getOrigin());
        flight.setDestination(request.getDestination());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setDuration(request.getDuration());
        flight.setPrice(request.getPrice());
        flight.setAvailableSeats(request.getAvailableSeats());
        flight.setStatus(request.getStatus() == null || request.getStatus().isBlank() ? "ON TIME" : request.getStatus());
        return FlightDTO.fromEntity(flightRepository.save(flight));
    }

    @Transactional
    public void deleteFlight(Long id) {
        flightRepository.delete(findEntity(id));
    }

    public Flight findEntity(Long id) {
        return flightRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Flight not found"));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank() && !"Any".equalsIgnoreCase(value);
    }

    private String valueOrEmpty(String value) {
        return hasText(value) ? value : "";
    }
}
