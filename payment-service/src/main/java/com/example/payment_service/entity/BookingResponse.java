package com.example.payment_service.entity;


import java.time.LocalDate;

public class BookingResponse {
    private Long bookingId;

    private String emailId;
    private String flightNumber;
    private String passengerName;
    private LocalDate bookingDate;
    private LocalDate journeyDate;
    private String status;

    private String seatNumber;

    public BookingResponse(String emailId, Long bookingId, String passengerName, String flightNumber, LocalDate bookingDate, LocalDate journeyDate, String status, String seatNumber) {
        this.emailId = emailId;
        this.bookingId = bookingId;
        this.passengerName = passengerName;
        this.flightNumber = flightNumber;
        this.bookingDate = LocalDate.now();
        this.journeyDate = journeyDate;
        this.status = status;
        this.seatNumber = seatNumber;
    }

    public String getSeatNumber() {
        return seatNumber;
    }
}