package com.example.flight_service.customexceptions;

public class NoFlightsAvailableException extends RuntimeException{
    public NoFlightsAvailableException(String message) {
        super(message);
    }
}
