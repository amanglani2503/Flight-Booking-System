package com.example.messaging_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessagingDetails implements Serializable {
    private String recipientEmail;
    private String passengerName;
    private String bookingId;
    private String flightId;
    private String departureAirport;
    private String arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String seatNumber;
    private double totalAmountPaid;
    private String status;
}
