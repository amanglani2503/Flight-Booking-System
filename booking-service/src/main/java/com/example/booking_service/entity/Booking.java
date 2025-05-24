package com.example.booking_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    private String emailId;

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotBlank(message = "Passenger name is required")
    private String passengerName;

    private LocalDateTime bookingDate;

    private String status;

    public Booking() {
        this.bookingDate = LocalDateTime.now();
        this.status = "PENDING";
    }
}
