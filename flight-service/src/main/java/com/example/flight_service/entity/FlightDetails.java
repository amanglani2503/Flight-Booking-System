package com.example.flight_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class FlightDetails {
    private String airlineName;;
    private String seatNumber;
    private String departureLocation;
    private String arrivalLocation;
    private LocalTime timeOfDeparture;
    private LocalTime timeOfArrival;
    private LocalDate travelDate;
    private LocalDate dateOfBooking;
    private double amountPaid;
}
