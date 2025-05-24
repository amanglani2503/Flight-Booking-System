package com.example.flight_service.customexceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation Error", message);
    }

    @ExceptionHandler(FlightNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFlightNotFound(FlightNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Flight Not Found", ex.getMessage());
    }

    @ExceptionHandler(InvalidFlightOperationException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFlightOperation(InvalidFlightOperationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid Flight Operation", ex.getMessage());
    }

    @ExceptionHandler(NoFlightsAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleNoFlightsAvailable(NoFlightsAvailableException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "No Flights Available", ex.getMessage());
    }

    @ExceptionHandler(SeatCancellationException.class)
    public ResponseEntity<Map<String, Object>> handleSeatCancellation(SeatCancellationException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Seat Cancellation Failed", ex.getMessage());
    }

    @ExceptionHandler(SeatNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleSeatNotAvailable(SeatNotAvailableException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Seat Not Available", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", status.value());
        errorDetails.put("error", error);
        errorDetails.put("message", message);
        return new ResponseEntity<>(errorDetails, status);
    }
}
