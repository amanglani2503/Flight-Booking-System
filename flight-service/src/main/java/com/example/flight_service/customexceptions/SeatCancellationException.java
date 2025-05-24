package com.example.flight_service.customexceptions;

public class SeatCancellationException extends RuntimeException {
    public SeatCancellationException(String message) {
        super(message);
    }
}
