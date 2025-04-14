package com.example.flight_service.service;

import com.example.flight_service.dto.FlightDTO;
import com.example.flight_service.entity.Flight;
import com.example.flight_service.entity.FlightDetails;
import com.example.flight_service.entity.Seat;
import com.example.flight_service.entity.SeatStatus;
import com.example.flight_service.repository.FlightRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);

    @Autowired
    private FlightRepository flightRepository;

    // Save a new flight
    public Flight addFlight(Flight flight) {
        logger.info("Adding new flight: {}", flight);
        return flightRepository.save(flight);
    }

    // Update existing flight details
    public Flight updateFlight(Integer id, Flight flightDetails) {
        logger.info("Updating flight with ID: {}", id);
        return flightRepository.findById(id)
                .map(flight -> {
                    flight.setAirline(flightDetails.getAirline());
                    flight.setDeparture(flightDetails.getDeparture());
                    flight.setDestination(flightDetails.getDestination());
                    flight.setDepartureTime(flightDetails.getDepartureTime());
                    flight.setArrivalTime(flightDetails.getArrivalTime());
                    flight.setAvailableSeats(flightDetails.getAvailableSeats());
                    flight.setPrice(flightDetails.getPrice());
                    return flightRepository.save(flight);
                })
                .orElseThrow(() -> {
                    logger.warn("Flight not found with ID: {}", id);
                    return new RuntimeException("Flight not found with ID: " + id);
                });
    }

    // Delete a flight by ID
    public Flight deleteFlight(Integer id) {
        logger.info("Deleting flight with ID: {}", id);
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Flight not found with ID: {}", id);
                    return new RuntimeException("Flight not found with ID: " + id);
                });
        flightRepository.deleteById(id);
        return flight;
    }

    // Get flights with available seats
    public List<FlightDTO> getAvailableFlights() {
        logger.debug("Fetching flights with available seats");
        List<Flight> availableFlights = flightRepository.findByAvailableSeatsGreaterThan(0);
        return availableFlights.stream()
                .map(flight -> FlightDTO.builder()
                        .id(flight.getId())
                        .airline(flight.getAirline())
                        .departure(flight.getDeparture())
                        .destination(flight.getDestination())
                        .departureTime(flight.getDepartureTime())
                        .arrivalTime(flight.getArrivalTime())
                        .availableSeats(flight.getAvailableSeats())
                        .price(flight.getPrice())
                        .build())
                .toList();
    }

    // Get all flights
    public List<FlightDTO> getAllFlights() {
        logger.debug("Fetching all flights");
        List<Flight> allFlights = flightRepository.findAll();
        return allFlights.stream()
                .map(flight -> FlightDTO.builder()
                        .id(flight.getId())
                        .airline(flight.getAirline())
                        .departure(flight.getDeparture())
                        .destination(flight.getDestination())
                        .departureTime(flight.getDepartureTime())
                        .arrivalTime(flight.getArrivalTime())
                        .availableSeats(flight.getAvailableSeats())
                        .price(flight.getPrice())
                        .build())
                .toList();
    }

    // Check seat availability for a flight
    public boolean isSeatAvailable(Integer flightId) {
        logger.debug("Checking seat availability for flight ID: {}", flightId);
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        return flightOpt.map(f -> f.getSeats().stream()
                        .anyMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE))
                .orElse(false);
    }

    // Book the first available seat
    public FlightDetails bookSeat(Integer flightId) {
        logger.info("Booking a seat on flight ID: {}", flightId);
        Optional<Flight> flightOpt = flightRepository.findById(flightId);
        if (flightOpt.isEmpty()) {
            logger.error("Flight not found with ID: {}", flightId);
            throw new RuntimeException("Flight not found!");
        }

        Flight flight = flightOpt.get();
        Optional<Seat> seatToBook = flight.getSeats().stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .findFirst();

        if (seatToBook.isPresent()) {
            Seat seat = seatToBook.get();
            seat.setStatus(SeatStatus.BOOKED);

            long updatedCount = flight.getSeats().stream()
                    .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                    .count();
            flight.setAvailableSeats((int) updatedCount);
            flightRepository.save(flight);

            logger.info("Seat {} booked on flight {}", seat.getSeatNumber(), flightId);

            return new FlightDetails(flight.getAirline(), seat.getSeatNumber(), flight.getDeparture(),
                    flight.getDestination(), flight.getDepartureTime(), flight.getArrivalTime(), flight.getPrice());
        } else {
            logger.warn("No available seats in flight ID: {}", flightId);
            throw new RuntimeException("No available seats in this flight!");
        }
    }

    // Cancel a booked seat
    public void cancelSeat(Integer flightId, String seatNumber) {
        logger.info("Cancelling seat {} on flight ID: {}", seatNumber, flightId);
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> {
                    logger.error("Flight not found with ID: {}", flightId);
                    return new RuntimeException("Flight not found with ID: " + flightId);
                });

        Optional<Seat> seatOpt = flight.getSeats().stream()
                .filter(seat -> seat.getSeatNumber().equals(seatNumber) && seat.getStatus() == SeatStatus.BOOKED)
                .findFirst();

        if (seatOpt.isPresent()) {
            Seat seat = seatOpt.get();
            seat.setStatus(SeatStatus.AVAILABLE);
            flight.setAvailableSeats(flight.getAvailableSeats() + 1);
            flightRepository.save(flight);
            logger.info("Seat {} cancelled successfully on flight ID: {}", seatNumber, flightId);
        } else {
            logger.warn("Seat {} not found or already available on flight ID: {}", seatNumber, flightId);
            throw new RuntimeException("Seat not found or already available!");
        }
    }

    // Fetch flight details by ID
    public FlightDetails getFlightDetailsById(Integer flightId) {
        logger.debug("Fetching flight details for ID: {}", flightId);
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> {
                    logger.error("Flight not found with ID: {}", flightId);
                    return new RuntimeException("Flight not found with ID: " + flightId);
                });

        return new FlightDetails(
                flight.getAirline(),
                null,
                flight.getDeparture(),
                flight.getDestination(),
                flight.getDepartureTime(),
                flight.getArrivalTime(),
                flight.getPrice()
        );
    }
}
