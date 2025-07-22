package com.example.booking_service.controller;

import com.example.booking_service.entity.Booking;
import com.example.booking_service.entity.BookingPaymentDTO;
import com.example.booking_service.entity.BookingResponse;
import com.example.booking_service.entity.StripeResponse;
import com.example.booking_service.service.BookingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingService bookingService;

    // Book a flight - input is Booking entity
    @PostMapping("/book")
    public ResponseEntity<StripeResponse> bookFlight(@Valid @RequestBody Booking booking) {
        logger.info("Received booking request for flight: {}", booking.getFlightCode());
        StripeResponse paymentResponse = bookingService.bookFlight(booking);
        logger.info("Flight booking initiated and payment response received for user: {}", booking.getEmail());
        return ResponseEntity.ok(paymentResponse);
    }

    // Cancel a booking by bookingId
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<String> cancelBooking(@PathVariable Long bookingId) {
        logger.info("Received request to cancel booking with ID: {}", bookingId);
        bookingService.cancelBooking(bookingId);
        logger.info("Booking with ID {} canceled successfully", bookingId);
        return ResponseEntity.ok("Booking canceled successfully.");
    }

    // Get booking details by bookingId
    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingResponse> getBookingDetails(@PathVariable Long bookingId) {
        logger.info("Fetching booking details for booking ID: {}", bookingId);
        BookingResponse booking = bookingService.getBookingDetails(bookingId);
        logger.info("Booking details retrieved for booking ID: {}", bookingId);
        return ResponseEntity.ok(booking);
    }

    // Get bookings for a user by their email
    @GetMapping("/user/{userEmail}")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable String userEmail) {
        logger.info("Fetching bookings for user email: {}", userEmail);
        List<BookingResponse> bookings = bookingService.getBookingsByUser(userEmail);
        logger.info("Retrieved {} bookings for user email: {}", bookings.size(), userEmail);
        return ResponseEntity.ok(bookings);
    }

    // Confirm and save a booking
    @PutMapping("/confirm")
    public ResponseEntity<String> confirmBooking(@RequestParam Long bookingId,
                                                 @RequestParam String seatNumber) {
        logger.info("Confirming booking with ID: {} and seat number: {}", bookingId, seatNumber);
        bookingService.saveConfirmedBooking(bookingId, seatNumber);
        logger.info("Booking with ID {} confirmed and saved with seat number {}", bookingId, seatNumber);
        return ResponseEntity.ok("Booking saved successfully");
    }
}
