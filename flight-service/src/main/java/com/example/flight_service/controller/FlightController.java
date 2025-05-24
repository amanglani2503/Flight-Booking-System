package com.example.flight_service.controller;

import com.example.flight_service.dto.FlightDTO;
import com.example.flight_service.entity.Flight;
import com.example.flight_service.entity.FlightDetails;
import com.example.flight_service.service.FlightService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {

    @Autowired
    private FlightService flightService;

    // Create a new flight (Admin only)
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Flight> addFlight(@Valid @RequestBody Flight flight) {
        Flight savedFlight = flightService.addFlight(flight);
        return new ResponseEntity<>(savedFlight, HttpStatus.CREATED);
    }

    // Update flight details by ID (Admin only)
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Flight> updateFlight(@PathVariable Integer id, @Valid @RequestBody Flight flight) {
        Flight updatedFlight = flightService.updateFlight(id, flight);
        return new ResponseEntity<>(updatedFlight, HttpStatus.OK);
    }

    // Delete a flight by ID (Admin only)
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Flight> deleteFlight(@PathVariable Integer id) {
        Flight deletedFlight = flightService.deleteFlight(id);
        return new ResponseEntity<>(deletedFlight, HttpStatus.OK);
    }

    // Retrieve all flights that have available seats
    @GetMapping("/available")
    public ResponseEntity<List<FlightDTO>> getAvailableFlights() {
        List<FlightDTO> availableFlights = flightService.getAvailableFlights();
        return new ResponseEntity<>(availableFlights, HttpStatus.OK);
    }

    // Retrieve all flights regardless of availability
    @GetMapping("/all")
    public ResponseEntity<List<FlightDTO>> getAllFlights() {
        List<FlightDTO> flights = flightService.getAllFlights();
        return new ResponseEntity<>(flights, HttpStatus.OK);
    }

    // Check seat availability for a specific flight (Passenger only)
    @GetMapping("/check-availability")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<Boolean> isSeatAvailable(@RequestParam Integer flightId) {
        boolean available = flightService.isSeatAvailable(flightId);
        return ResponseEntity.ok(available);
    }

    // Book a seat in a specific flight (Passenger only)
    @PutMapping("/book-seats")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<FlightDetails> bookSeat(@RequestParam Integer flightId) {
        FlightDetails details = flightService.bookSeat(flightId);
        return ResponseEntity.ok(details);
    }

    // Cancel a seat in a specific flight
    @PutMapping("/cancel-seat")
    public ResponseEntity<String> cancelSeat(@RequestParam Integer flightId, @RequestParam String seatNumber) {
        flightService.cancelSeat(flightId, seatNumber);
        return ResponseEntity.ok("Seat cancellation successful!");
    }

    // Get detailed flight information by flight ID
    @GetMapping("/getDetails")
    public ResponseEntity<FlightDetails> getDetails(@RequestParam Integer flightId) {
        FlightDetails details = flightService.getFlightDetailsById(flightId);
        return ResponseEntity.ok(details);
    }
}
