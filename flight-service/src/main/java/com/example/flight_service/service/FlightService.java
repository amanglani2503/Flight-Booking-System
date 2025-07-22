package com.example.flight_service.service;

import com.example.flight_service.customexceptions.FlightNotFoundException;
import com.example.flight_service.customexceptions.SeatCancellationException;
import com.example.flight_service.customexceptions.SeatNotAvailableException;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class FlightService {

    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);

    @Autowired
    private FlightRepository flightRepository;

    public void addFlight(FlightDTO flightDTO) {
        logger.info("Adding flight with code: {}, schedule type: {}", flightDTO.getFlightCode(), flightDTO.getScheduleType());
        switch (flightDTO.getScheduleType()) {
            case DAILY -> scheduleDaily(flightDTO);
            case WEEKLY -> scheduleWeekly(flightDTO);
            case MONTHLY -> scheduleMonthly(flightDTO);
            case CUSTOM_DATES -> scheduleCustomDates(flightDTO);
            default -> logger.warn("Unknown schedule type: {}", flightDTO.getScheduleType());
        }
    }

    private void scheduleDaily(FlightDTO flightDTO) {
        logger.debug("Scheduling daily flights for flight code {}", flightDTO.getFlightCode());
        LocalDate current = flightDTO.getStartDate();
        LocalDate end = flightDTO.getEndDate();

        while (current != null && !current.isAfter(end)) {
            saveFlightInstance(flightDTO, current);
            current = current.plusDays(1);
        }
    }

    private void scheduleWeekly(FlightDTO flightDTO) {
        logger.debug("Scheduling weekly flights for flight code {}", flightDTO.getFlightCode());
        LocalDate current = flightDTO.getStartDate();
        LocalDate end = flightDTO.getEndDate();

        while (current != null && !current.isAfter(end)) {
            int dayOfWeek = current.getDayOfWeek().getValue();
            if (flightDTO.getRunningDaysOfWeek() != null && flightDTO.getRunningDaysOfWeek().contains(dayOfWeek)) {
                saveFlightInstance(flightDTO, current);
            }
            current = current.plusDays(1);
        }
    }

    private void scheduleMonthly(FlightDTO flightDTO) {
        logger.debug("Scheduling monthly flights for flight code {}", flightDTO.getFlightCode());
        LocalDate current = flightDTO.getStartDate();
        LocalDate end = flightDTO.getEndDate();

        while (current != null && !current.isAfter(end)) {
            int dayOfMonth = current.getDayOfMonth();
            if (flightDTO.getDaysOfMonth() != null && flightDTO.getDaysOfMonth().contains(dayOfMonth)) {
                saveFlightInstance(flightDTO, current);
            }
            current = current.plusDays(1);
        }
    }

    private void scheduleCustomDates(FlightDTO flightDTO) {
        logger.debug("Scheduling custom dates for flight code {}", flightDTO.getFlightCode());
        if (flightDTO.getSpecificDates() != null) {
            for (LocalDate date : flightDTO.getSpecificDates()) {
                saveFlightInstance(flightDTO, date);
            }
        }
    }

    private void saveFlightInstance(FlightDTO flightDTO, LocalDate scheduledDate) {
        logger.info("Saving flight instance for flight code {} on {}", flightDTO.getFlightCode(), scheduledDate);
        Flight flight = Flight.builder()
                .flightCode(flightDTO.getFlightCode())
                .airlineName(flightDTO.getCarrierName())
                .departureCity(flightDTO.getDepartureCity())
                .arrivalCity(flightDTO.getArrivalCity())
                .timeOfDeparture(flightDTO.getTimeOfDeparture())
                .timeOfArrival(flightDTO.getTimeOfArrival())
                .seatCapacity(flightDTO.getSeatCapacity())
                .seatsAvailable(flightDTO.getSeatCapacity())
                .ticketPrice(flightDTO.getTicketPrice())
                .departureDay(scheduledDate)
                .weekday(scheduledDate.getDayOfWeek().name())
                .build();

        List<Seat> seatList = new ArrayList<>();
        int rows = (int) Math.ceil((double) flight.getSeatCapacity() / 6);
        for (int row = 1; row <= rows; row++) {
            for (char col = 'A'; col <= 'F'; col++) {
                int seatIndex = (row - 1) * 6 + (col - 'A' + 1);
                if (seatIndex > flight.getSeatCapacity()) break;

                Seat seat = Seat.builder()
                        .seatNumber(row + String.valueOf(col))
                        .status(SeatStatus.AVAILABLE)
                        .flight(flight)
                        .build();
                seatList.add(seat);
            }
        }
        flight.setSeatList(seatList);

        flightRepository.save(flight);
        logger.debug("Flight instance saved for flight code {} on {}", flightDTO.getFlightCode(), scheduledDate);
    }

    public void deleteFlight(String flightCode) {
        logger.info("Deleting flights with flight code: {}", flightCode);
        List<Flight> flights = flightRepository.findByFlightCode(flightCode);

        if (flights.isEmpty()) {
            logger.error("No flights found with flight code: {}", flightCode);
            throw new FlightNotFoundException("No flights found with flight code: " + flightCode);
        }

        flightRepository.deleteAll(flights);
        logger.info("Deleted {} flights with flight code {}", flights.size(), flightCode);
    }

    public List<Flight> getAvailableFlights() {
        logger.info("Fetching all available flights");
        return flightRepository.findBySeatsAvailableGreaterThan(0);
    }

    public List<Flight> getAllFlights() {
        logger.info("Fetching all flights");
        return flightRepository.findAll();
    }

    public boolean isSeatAvailable(String flightCode, LocalDate travelDate) {
        logger.info("Checking seat availability for flight code {} on {}", flightCode, travelDate);
        Optional<Flight> optionalFlight = flightRepository.findByFlightCodeAndDepartureDay(flightCode, travelDate);

        if (optionalFlight.isEmpty()) {
            logger.error("No flight found with flight code {} on {}", flightCode, travelDate);
            throw new FlightNotFoundException("No flight found with flight code " + flightCode + " on " + travelDate);
        }

        Flight flight = optionalFlight.get();
        boolean available = flight.getSeatsAvailable() > 0;
        logger.info("Seat availability for flight code {} on {}: {}", flightCode, travelDate, available);
        return available;
    }

    public FlightDetails bookSeat(String flightCode, LocalDate travelDate) {
        logger.info("Booking seat for flight code {} on {}", flightCode, travelDate);
        Flight flight = flightRepository.findByFlightCodeAndDepartureDay(flightCode, travelDate)
                .orElseThrow(() -> {
                    logger.error("No flight found with code {} on {}", flightCode, travelDate);
                    return new FlightNotFoundException("No flight found with code " + flightCode + " on " + travelDate);
                });

        if (flight.getSeatsAvailable() <= 0) {
            logger.error("No seats available for flight {} on {}", flightCode, travelDate);
            throw new SeatNotAvailableException("No seats available for flight " + flightCode + " on " + travelDate);
        }

        Optional<Seat> optionalSeat = flight.getSeatList().stream()
                .filter(seat -> seat.getStatus() == SeatStatus.AVAILABLE)
                .findFirst();

        if (optionalSeat.isEmpty()) {
            logger.error("All seats are booked for flight {}", flightCode);
            throw new SeatNotAvailableException("All seats are booked for flight " + flightCode);
        }

        Seat seat = optionalSeat.get();
        seat.setStatus(SeatStatus.BOOKED);
        flight.setSeatsAvailable(flight.getSeatsAvailable() - 1);

        flightRepository.save(flight);
        logger.info("Seat {} booked for flight {} on {}", seat.getSeatNumber(), flightCode, travelDate);

        return new FlightDetails(
                flight.getAirlineName(),
                seat.getSeatNumber(),
                flight.getDepartureCity(),
                flight.getArrivalCity(),
                flight.getTimeOfDeparture(),
                flight.getTimeOfArrival(),
                flight.getDepartureDay(),
                LocalDate.now(),
                flight.getTicketPrice()
        );
    }

    public void cancelSeat(String flightCode, LocalDate travelDate, String seatId) {
        logger.info("Cancelling seat {} for flight code {} on {}", seatId, flightCode, travelDate);
        Flight flight = flightRepository.findByFlightCodeAndDepartureDay(flightCode, travelDate)
                .orElseThrow(() -> {
                    logger.error("No flight found with code {} on {}", flightCode, travelDate);
                    return new FlightNotFoundException("No flight found with code " + flightCode + " on " + travelDate);
                });

        Seat seat = flight.getSeatList().stream()
                .filter(s -> s.getSeatNumber().equalsIgnoreCase(seatId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.error("Seat {} not found in flight {} on {}", seatId, flightCode, travelDate);
                    return new SeatNotAvailableException("Seat " + seatId + " not found in flight " + flightCode + " on " + travelDate);
                });

        if (seat.getStatus() != SeatStatus.BOOKED) {
            logger.error("Seat {} is not booked and cannot be cancelled.", seatId);
            throw new SeatCancellationException("Seat " + seatId + " is not currently booked.");
        }

        seat.setStatus(SeatStatus.AVAILABLE);
        flight.setSeatsAvailable(flight.getSeatsAvailable() + 1);

        flightRepository.save(flight);
        logger.info("Seat {} cancelled for flight code {} on {}", seatId, flightCode, travelDate);
    }

    public FlightDetails getFlightDetails(String flightCode, LocalDate travelDate) {
        logger.info("Getting flight details for flight code {} on {}", flightCode, travelDate);
        Flight flight = flightRepository.findByFlightCodeAndDepartureDay(flightCode, travelDate)
                .orElseThrow(() -> {
                    logger.error("No flight found with code {} on {}", flightCode, travelDate);
                    return new FlightNotFoundException("No flight found with code " + flightCode + " on " + travelDate);
                });

        return new FlightDetails(
                flight.getAirlineName(),
                null,
                flight.getDepartureCity(),
                flight.getArrivalCity(),
                flight.getTimeOfDeparture(),
                flight.getTimeOfArrival(),
                flight.getDepartureDay(),
                LocalDate.now(),
                flight.getTicketPrice()
        );
    }
}
