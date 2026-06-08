package com.skyjet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Airline Entity - Maps to the airlines table
 */
@Entity
@Table(name = "airlines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "airline_id")
    private Long airlineId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "iata_code", unique = true, nullable = false, length = 2, columnDefinition = "bpchar(2)")
    private String iataCode;
}
