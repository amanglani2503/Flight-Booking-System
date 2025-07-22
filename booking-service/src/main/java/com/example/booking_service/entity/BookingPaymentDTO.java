package com.example.booking_service.entity;

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
    private String email;
    private String passengerName;
    private String flightNumber;
    private LocalDate dateOfBooking;
    private LocalDate journeyDate;
    private String seatNumber;

    // Payment fields
    private double amount;
    private String currency;
}
