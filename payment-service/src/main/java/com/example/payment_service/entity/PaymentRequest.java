package com.example.payment_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    private double amount;
    private String flightNumber;
    private String currency;
    private String seatNumber;
    private String bookingId;
    private String userId;
}
