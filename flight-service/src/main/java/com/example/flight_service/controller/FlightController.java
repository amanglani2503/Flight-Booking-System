//package com.example.flight_service.controller;
//
//import com.example.flight_service.dto.FlightDTO;
//import com.example.flight_service.entity.Flight;
//import com.example.flight_service.entity.FlightDetails;
//import com.example.flight_service.service.FlightService;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDate;
//import java.util.List;
//
//@RestController
//@RequestMapping("/flights")
//public class FlightController {
//
//    @Autowired
//    private FlightService flightService;
//
//    // Create a new flight (Admin only)
//    @PostMapping("/add")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<String> addFlight(@Valid @RequestBody FlightDTO flightDTO) {
//        flightService.addFlight(flightDTO);
//        return ResponseEntity.status(HttpStatus.CREATED).body("Flight(s) scheduled successfully");
//    }
//
////     Update flight details by ID (Admin only)
//    @PutMapping("/update/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<String> updateFlight(@PathVariable Integer id, @Valid @RequestBody FlightDTO flightDTO) {
////        flightService.updateFlight(id, flightDTO);
//        return ResponseEntity.status(HttpStatus.OK).body("Flights updated successfully");
//    }
//
//    // Delete a flight by ID (Admin only)
//    @DeleteMapping("/delete/{flightNumber}")
//    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<String> deleteFlight(@PathVariable String flightNumber) {
//        flightService.deleteFlight(flightNumber);
//        return ResponseEntity.status(HttpStatus.OK).body("Flights deleted successfully");
//    }
//
//    // Retrieve all flights that have available seats
//    @GetMapping("/available")
//    public ResponseEntity<List<Flight>> getAvailableFlights() {
//        List<Flight> availableFlights = flightService.getAvailableFlights();
//        System.out.println("Lengths - " + availableFlights.size());
//        return new ResponseEntity<>(availableFlights, HttpStatus.OK);
//    }
//
//    // Retrieve all flights regardless of availability
//    @GetMapping("/all")
//    public ResponseEntity<List<Flight>> getAllFlights() {
//        return ResponseEntity.status(HttpStatus.OK).body(flightService.getAllFlights());
//    }
//
//    // Check seat availability for a specific flight (Passenger only)
//    @GetMapping("/check-availability")
//    @PreAuthorize("hasRole('PASSENGER')")
//    public boolean isSeatAvailable(@RequestParam String flightNumber, @RequestParam LocalDate journeyDate) {
//        return flightService.isSeatAvailable(flightNumber, journeyDate);
//
//    }
//
//    // Book a seat in a specific flight (Passenger only)
//    @PutMapping("/book-seats")
//    @PreAuthorize("hasRole('PASSENGER')")
//    public ResponseEntity<FlightDetails> bookSeat(@RequestParam String flightNumber, @RequestParam LocalDate journeyDate) {
//        FlightDetails details = flightService.bookSeat(flightNumber, journeyDate);
//        return ResponseEntity.ok(details);
//    }
//
//    // Cancel a seat in a specific flight
//    @PutMapping("/cancel-seat")
//    public ResponseEntity<String> cancelSeat(@RequestParam String flightNumber, @RequestParam LocalDate journeyDate,@RequestParam String seatNumber) {
//        flightService.cancelSeat(flightNumber, journeyDate, seatNumber);
//        return ResponseEntity.ok("Seat cancellation successful!");
//    }
//
//    // Get detailed flight information by flight ID
//    @GetMapping("/getDetails")
//    public ResponseEntity<FlightDetails> getDetails(@RequestParam String flightNumber, @RequestParam LocalDate journeyDate) {
//        FlightDetails details = flightService.getFlightDetails(flightNumber, journeyDate);
//        return ResponseEntity.ok(details);
//    }
//}

package com.example.flight_service.controller;

import com.example.flight_service.dto.FlightDTO;
import com.example.flight_service.entity.Flight;
import com.example.flight_service.entity.FlightDetails;
import com.example.flight_service.service.FlightService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {

    private static final Logger logger = LoggerFactory.getLogger(FlightController.class);

    @Autowired
    private FlightService flightService;

    // Create a new flight (Admin only)
    @PostMapping("/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addFlight(@Valid @RequestBody FlightDTO flightDTO) {
        logger.info("Request received to add new flight: {}", flightDTO.getFlightCode());
        flightService.addFlight(flightDTO);
        logger.info("Flight added successfully: {}", flightDTO.getFlightCode());
        return ResponseEntity.status(HttpStatus.CREATED).body("Flight(s) scheduled successfully");
    }

    // Update flight details by ID (Admin only)
    @PutMapping("/update/{flightId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> updateFlight(@PathVariable Integer flightId, @Valid @RequestBody FlightDTO flightDTO) {
        logger.info("Request received to update flight with ID: {}", flightId);
        // flightService.updateFlight(flightId, flightDTO);
        logger.info("Flight updated successfully with ID: {}", flightId);
        return ResponseEntity.status(HttpStatus.OK).body("Flights updated successfully");
    }

    // Delete a flight by flight code (Admin only)
    @DeleteMapping("/delete/{flightCode}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteFlight(@PathVariable String flightCode) {
        logger.info("Request received to delete flight with code: {}", flightCode);
        flightService.deleteFlight(flightCode);
        logger.info("Flight deleted successfully: {}", flightCode);
        return ResponseEntity.status(HttpStatus.OK).body("Flights deleted successfully");
    }

    // Retrieve all flights that have available seats
    @GetMapping("/available")
    public ResponseEntity<List<Flight>> getAvailableFlights() {
        logger.info("Request received to get available flights");
        List<Flight> availableFlights = flightService.getAvailableFlights();
        logger.info("Returning {} available flights", availableFlights.size());
        return new ResponseEntity<>(availableFlights, HttpStatus.OK);
    }

    // Retrieve all flights regardless of availability
    @GetMapping("/all")
    public ResponseEntity<List<Flight>> getAllFlights() {
        logger.info("Request received to get all flights");
        List<Flight> allFlights = flightService.getAllFlights();
        logger.info("Returning {} flights", allFlights.size());
        return ResponseEntity.status(HttpStatus.OK).body(allFlights);
    }

    // Check seat availability for a specific flight (Passenger only)
    @GetMapping("/check-availability")
    @PreAuthorize("hasRole('PASSENGER')")
    public boolean isSeatAvailable(@RequestParam String flightCode, @RequestParam LocalDate travelDate) {
        logger.info("Checking seat availability for flight: {}, date: {}", flightCode, travelDate);
        boolean available = flightService.isSeatAvailable(flightCode, travelDate);
        logger.info("Seat availability for flight {} on {}: {}", flightCode, travelDate, available);
        return available;
    }

    // Book a seat in a specific flight (Passenger only)
    @PutMapping("/book-seats")
    @PreAuthorize("hasRole('PASSENGER')")
    public ResponseEntity<FlightDetails> bookSeat(@RequestParam String flightCode, @RequestParam LocalDate travelDate) {
        logger.info("Booking seat for flight: {}, date: {}", flightCode, travelDate);
        FlightDetails details = flightService.bookSeat(flightCode, travelDate);
        logger.info("Seat booked successfully for flight: {}, date: {}", flightCode, travelDate);
        return ResponseEntity.ok(details);
    }

    // Cancel a seat in a specific flight
    @PutMapping("/cancel-seat")
    public ResponseEntity<String> cancelSeat(@RequestParam String flightCode, @RequestParam LocalDate travelDate, @RequestParam String seatId) {
        logger.info("Request received to cancel seat {} for flight {} on date {}", seatId, flightCode, travelDate);
        flightService.cancelSeat(flightCode, travelDate, seatId);
        logger.info("Seat cancellation successful for seat {} on flight {} at {}", seatId, flightCode, travelDate);
        return ResponseEntity.ok("Seat cancellation successful!");
    }

    // Get detailed flight information by flight code and date
    @GetMapping("/getDetails")
    public ResponseEntity<FlightDetails> getDetails(@RequestParam String flightCode, @RequestParam LocalDate travelDate) {
        logger.info("Fetching flight details for flight: {}, date: {}", flightCode, travelDate);
        FlightDetails details = flightService.getFlightDetails(flightCode, travelDate);
        logger.info("Returning flight details for flight: {}, date: {}", flightCode, travelDate);
        return ResponseEntity.ok(details);
    }
}
