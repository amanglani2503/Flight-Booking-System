package com.example.booking_service.controller;

import com.example.booking_service.entity.Booking;
import com.example.booking_service.entity.BookingResponse;
import com.example.booking_service.entity.StripeResponse;
import com.example.booking_service.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/book")
    public ResponseEntity<?> bookFlight(@Valid @RequestBody Booking booking) {
            StripeResponse paymentResponse = bookingService.bookFlight(booking);
            return ResponseEntity.ok(paymentResponse);
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
            bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok("Booking canceled successfully.");

    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<?> getBookingDetails(@PathVariable Long bookingId) {
            BookingResponse booking = bookingService.getBookingDetails(bookingId);
            return ResponseEntity.ok(booking);

    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBookingsByUser(@PathVariable String userId) {
        List<BookingResponse> bookings = bookingService.getBookingsByUser(userId);
        return ResponseEntity.ok(bookings);
    }
}
