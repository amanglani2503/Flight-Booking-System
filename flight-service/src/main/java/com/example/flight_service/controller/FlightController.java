package com.example.flight_service.controller;

import com.example.flight_service.dto.FlightDTO;
import com.example.flight_service.entity.Flight;
import com.example.flight_service.entity.FlightDetails;
import com.example.flight_service.service.FlightService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {

    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);

    @Autowired
    private FlightService flightService;

    // Create a new flight (Admin only)
    @PostMapping("add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Flight> addFlight(@RequestBody Flight flight) {
        try {
            Flight savedFlight = flightService.addFlight(flight);
            logger.info("Flight created with ID: {}", savedFlight.getId());
            return new ResponseEntity<>(savedFlight, HttpStatus.CREATED);
        } catch (Exception e) {
            logger.error("Error creating flight", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Update flight details by ID (Admin only)
    @PutMapping("update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Flight> updateFlight(@PathVariable Integer id, @RequestBody Flight flightDetails) {
        try {
            Flight updatedFlight = flightService.updateFlight(id, flightDetails);
            logger.info("Flight updated with ID: {}", id);
            return new ResponseEntity<>(updatedFlight, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.warn("Flight not found for update: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error updating flight with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Delete a flight by ID (Admin only)
    @DeleteMapping("delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Flight> deleteFlight(@PathVariable Integer id) {
        try {
            Flight deletedFlight = flightService.deleteFlight(id);
            logger.info("Flight deleted with ID: {}", id);
            return new ResponseEntity<>(deletedFlight, HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.warn("Flight not found for deletion: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error deleting flight with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Retrieve all flights that have available seats
    @GetMapping("available")
    public ResponseEntity<List<FlightDTO>> getAvailableFlights() {
        try {
            List<FlightDTO> availableFlights = flightService.getAvailableFlights();
            logger.info("Retrieved available flights");
            return new ResponseEntity<>(availableFlights, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching available flights", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Retrieve all flights regardless of availability
    @GetMapping("/all")
    public ResponseEntity<List<FlightDTO>> getAllFlights() {
        try {
            List<FlightDTO> flights = flightService.getAllFlights();
            logger.info("Retrieved all flights");
            return new ResponseEntity<>(flights, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching all flights", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Check seat availability for a specific flight (Passenger only)
    @GetMapping("/check-availability")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Boolean> isSeatAvailable(@RequestParam Integer flightId) {
        try {
            boolean available = flightService.isSeatAvailable(flightId);
            logger.info("Checked seat availability for flight ID: {}", flightId);
            return ResponseEntity.ok(available);
        } catch (Exception e) {
            logger.error("Error checking seat availability for flight ID: {}", flightId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Book a seat in a specific flight (Passenger only)
    @PutMapping("/book-seats")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<FlightDetails> bookSeat(@RequestParam Integer flightId) {
        try {
            FlightDetails details = flightService.bookSeat(flightId);
            logger.info("Seat booked for flight ID: {}", flightId);
            return ResponseEntity.ok(details);
        } catch (RuntimeException e) {
            logger.warn("Booking failed: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Error booking seat for flight ID: {}", flightId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Cancel a seat in a specific flight
    @PutMapping("/cancel-seat")
    public ResponseEntity<String> cancelSeat(@RequestParam Integer flightId, @RequestParam String seatNumber) {
        try {
            flightService.cancelSeat(flightId, seatNumber);
            logger.info("Seat {} cancelled in flight ID: {}", seatNumber, flightId);
            return ResponseEntity.ok("Seat cancellation successful!");
        } catch (RuntimeException e) {
            logger.warn("Seat cancellation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error cancelling seat {} in flight ID: {}", seatNumber, flightId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }

    // Get detailed flight information by flight ID
    @GetMapping("getDetails")
    public ResponseEntity<FlightDetails> getDetails(@RequestParam Integer flightId) {
        try {
            FlightDetails details = flightService.getFlightDetailsById(flightId);
            logger.info("Fetched flight details for ID: {}", flightId);
            return ResponseEntity.ok(details);
        } catch (RuntimeException e) {
            logger.warn("Flight not found with ID: {}", flightId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            logger.error("Error retrieving flight details for ID: {}", flightId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
