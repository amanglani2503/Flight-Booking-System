package com.example.booking_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagingDetails {
    private String recipientEmail;
    private String passengerName;
    private Long bookingId;
    private String flightNumber;
    private String departureAirport;
    private String arrivalAirport;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private LocalDate journeyDate;
    private LocalDate bookedOn;
    private String seatNumber;
    private double totalAmountPaid;
    private String bookingStatus;
}
