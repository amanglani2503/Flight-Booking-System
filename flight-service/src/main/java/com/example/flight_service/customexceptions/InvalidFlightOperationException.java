package com.example.flight_service.customexceptions;

public class InvalidFlightOperationException extends RuntimeException {
    public InvalidFlightOperationException(String message) {
        super(message);
    }
}