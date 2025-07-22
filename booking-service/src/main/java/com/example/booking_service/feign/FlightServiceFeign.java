package com.example.booking_service.feign;

import com.example.booking_service.entity.FlightDetails;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@FeignClient(name = "FLIGHT-SERVICE") // Communicates with the flight service
public interface FlightServiceFeign {

    // Check seat availability for a flight
    @GetMapping("/flights/check-availability")
    boolean isSeatAvailable(@RequestParam String flightCode,
                            @RequestParam LocalDate travelDate,
                            @RequestHeader("Authorization") String token);

    // Book a seat in the flight
    @PutMapping("/flights/book-seats")
    FlightDetails bookSeat(@RequestParam String flightCode,
                           @RequestParam LocalDate travelDate,
                           @RequestHeader("Authorization") String token);

    // Cancel a booked seat in the flight
    @PutMapping("/flights/cancel-seat")
    void cancelSeat(@RequestParam String flightCode,
                    @RequestParam LocalDate travelDate,
                    @RequestParam String seatNumber,
                    @RequestHeader("Authorization") String authHeader);

    // Fetch flight details by flight ID
    @GetMapping("/flights/getDetails")
    FlightDetails getFlightDetails(@RequestParam String flightCode,
                                   @RequestParam LocalDate travelDate,
                                   @RequestHeader("Authorization") String authHeader);
}
