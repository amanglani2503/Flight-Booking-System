package com.example.flight_service.repository;

import com.example.flight_service.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlightRepository extends JpaRepository<Flight, Integer> {
    List<Flight> findBySeatsAvailableGreaterThan(int seats);
    List<Flight> findByFlightCode(String flightNumber);
    Optional<Flight> findByFlightCodeAndDepartureDay(String flightNumber, LocalDate departureDate);
}