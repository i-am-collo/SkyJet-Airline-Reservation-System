package com.skyjet.backend.controller;

import com.skyjet.backend.dto.FlightDTO;
import com.skyjet.backend.service.FlightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping
    public List<FlightDTO> flights(@RequestParam(required = false) String origin,
                                   @RequestParam(required = false) String destination) {
        return flightService.findFlights(origin, destination);
    }

    @GetMapping("/{id}")
    public FlightDTO flight(@PathVariable Long id) {
        return flightService.getFlight(id);
    }
}
