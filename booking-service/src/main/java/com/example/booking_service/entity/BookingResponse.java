package com.example.booking_service.entity;

import jakarta.persistence.*;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
public class BookingResponse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String flightNumber;
    private String passengerName;
    private LocalDate dateOfBooking;
    private LocalDate journeyDate;
    private String bookingStatus;

    @Getter
    private String seatNumber;

    public BookingResponse(String email, Long id, String passengerName, String flightNumber, LocalDate dateOfBooking, LocalDate journeyDate, String bookingStatus, String seatNumber) {
        this.email = email;
        this.id = id;
        this.passengerName = passengerName;
        this.flightNumber = flightNumber;
        this.dateOfBooking = LocalDate.now();
        this.journeyDate = journeyDate;
        this.bookingStatus = bookingStatus;
        this.seatNumber = seatNumber;
    }
}
