package com.skyjet.backend.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Airport Entity - Maps to the airports table
 */
@Entity
@Table(name = "airports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Airport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "airport_id")
    private Long airportId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(name = "iata_code", unique = true, nullable = false, length = 3, columnDefinition = "CHAR(3)")
    private String iataCode;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String country;
}
