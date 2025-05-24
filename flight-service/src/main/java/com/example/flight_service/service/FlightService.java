package com.example.flight_service.service;

import com.example.flight_service.customexceptions.*;
import com.example.flight_service.dto.FlightDTO;
import com.example.flight_service.entity.Flight;
import com.example.flight_service.entity.FlightDetails;
import com.example.flight_service.entity.Seat;
import com.example.flight_service.entity.SeatStatus;
import com.example.flight_service.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    @Autowired
    private FlightRepository flightRepository;

    public Flight addFlight(Flight flight) {
        return flightRepository.save(flight);
    }

    public Flight updateFlight(Integer id, Flight updatedFlightDetails) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + id + " does not exist"));

        flight.setAirline(updatedFlightDetails.getAirline());
        flight.setDeparture(updatedFlightDetails.getDeparture());
        flight.setDestination(updatedFlightDetails.getDestination());
        flight.setDepartureTime(updatedFlightDetails.getDepartureTime());
        flight.setArrivalTime(updatedFlightDetails.getArrivalTime());
        flight.setAvailableSeats(updatedFlightDetails.getAvailableSeats());
        flight.setPrice(updatedFlightDetails.getPrice());

        return flightRepository.save(flight);
    }

    public Flight deleteFlight(Integer id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + id + " does not exist"));
        flightRepository.deleteById(id);
        return flight;
    }

    public List<FlightDTO> getAvailableFlights() {
        List<Flight> availableFlights = flightRepository.findByAvailableSeatsGreaterThan(0);

        if (availableFlights.isEmpty()) {
            throw new NoFlightsAvailableException("No flights available");
        }

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

    public List<FlightDTO> getAllFlights() {
        List<Flight> allFlights = flightRepository.findAll();
        if (allFlights.isEmpty()) {
            throw new NoFlightsAvailableException("No flights available");
        }

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

    public boolean isSeatAvailable(Integer flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + flightId + " does not exist"));

        return flight.getSeats().stream()
                .anyMatch(seat -> seat.getStatus() == SeatStatus.AVAILABLE);
    }

    public FlightDetails bookSeat(Integer flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + flightId + " does not exist"));

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

            return new FlightDetails(flight.getAirline(), seat.getSeatNumber(), flight.getDeparture(),
                    flight.getDestination(), flight.getDepartureTime(), flight.getArrivalTime(), flight.getPrice());
        } else {
            throw new SeatNotAvailableException("No available seats in this flight!");
        }
    }

    public void cancelSeat(Integer flightId, String seatNumber) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + flightId + " does not exist"));

        Optional<Seat> seatOpt = flight.getSeats().stream()
                .filter(seat -> seat.getSeatNumber().equals(seatNumber))
                .findFirst();

        if (seatOpt.isPresent()) {
            Seat seat = seatOpt.get();
            if (seat.getStatus() == SeatStatus.BOOKED) {
                seat.setStatus(SeatStatus.AVAILABLE);

                long updatedCount = flight.getSeats().stream()
                        .filter(s -> s.getStatus() == SeatStatus.AVAILABLE)
                        .count();
                flight.setAvailableSeats((int) updatedCount);
                flightRepository.save(flight);
            } else {
                throw new SeatCancellationException("Seat is not booked, cannot cancel");
            }
        } else {
            throw new SeatCancellationException("Seat number not found in flight");
        }
    }

    public FlightDetails getFlightDetailsById(Integer flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight with ID " + flightId + " does not exist"));

        return new FlightDetails(flight.getAirline(), null, flight.getDeparture(), flight.getDestination(),
                flight.getDepartureTime(), flight.getArrivalTime(), flight.getPrice());
    }
}
