package com.example.booking_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Flight number is required")
    private String flightCode;

    @NotBlank(message = "Passenger name is required")
    private String passengerName;

    @NotNull(message = "Journey date is required")
    private LocalDate travelDate;

    private LocalDate dateOfBooking;

    private String bookingStatus;

    public Booking() {
        this.dateOfBooking = LocalDate.now();
        this.bookingStatus = "PENDING";
    }
}
