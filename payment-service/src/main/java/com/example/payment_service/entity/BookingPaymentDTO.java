package com.example.payment_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingPaymentDTO {
    // Booking fields
    private Long bookingId;
    private String emailId;
    private String passengerName;
    private String flightNumber;
    private LocalDate bookingDate;
    private LocalDate journeyDate;
    private String seatNumber;

    // Payment fields
    private double amount;
    private String currency;
}