package com.skyjet.backend.controller;

import com.skyjet.backend.dto.FlightDTO;
import com.skyjet.backend.entity.Airport;
import com.skyjet.backend.repository.AirportRepository;
import com.skyjet.backend.service.FlightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FlightController {

    private final FlightService flightService;
    private final AirportRepository airportRepository;

    public FlightController(FlightService flightService, AirportRepository airportRepository) {
        this.flightService = flightService;
        this.airportRepository = airportRepository;
    }

    @GetMapping("/flights")
    public List<FlightDTO> flights(@RequestParam(required = false) String origin,
                                   @RequestParam(required = false) String destination) {
        return flightService.findFlights(origin, destination);
    }

    @GetMapping("/flights/{id}")
    public FlightDTO flight(@PathVariable Long id) {
        return flightService.getFlight(id);
    }

    /**
     * Returns all airports for search dropdown population.
     * Each entry includes city and IATA code.
     */
    @GetMapping("/airports")
    public List<Map<String, String>> airports() {
        return airportRepository.findAll().stream()
                .map(a -> Map.of(
                        "city", a.getCity(),
                        "iataCode", a.getIataCode().trim(),
                        "name", a.getName(),
                        "country", a.getCountry()))
                .toList();
    }
}
