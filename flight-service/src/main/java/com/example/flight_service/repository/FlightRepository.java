package com.example.flight_service.repository;

import com.example.flight_service.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlightRepository extends JpaRepository<Flight, Integer> {
    List<Flight> findByAvailableSeatsGreaterThan(int seats);
}