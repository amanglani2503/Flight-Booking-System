package com.example.booking_service.customexceptions;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
}
