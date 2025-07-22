package com.example.flight_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "flight")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long flightId;

    private String flightCode;
    private String airlineName;;
    private String departureCity;
    private String arrivalCity;
    private LocalTime timeOfDeparture;
    private LocalTime timeOfArrival;
    private int seatCapacity;
    private int seatsAvailable;
    private double ticketPrice;
    private LocalDate departureDay;
    private String weekday;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Seat> seatList = new ArrayList<>();
}
