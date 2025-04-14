package com.example.flight_service.service;

import com.example.flight_service.entity.Flight;
import com.example.flight_service.entity.FlightDetails;
import com.example.flight_service.entity.Seat;
import com.example.flight_service.entity.SeatStatus;
import com.example.flight_service.repository.FlightRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {

    @InjectMocks
    private FlightService flightService;

    @Mock
    private FlightRepository flightRepository;

    @Test
    void testAddFlight_ShouldSaveAndReturnFlight() {
        Flight flight = new Flight();
        when(flightRepository.save(flight)).thenReturn(flight);

        Flight result = flightService.addFlight(flight);

        assertEquals(flight, result);
        verify(flightRepository, times(1)).save(flight);
    }

    @Test
    void testUpdateFlight_ShouldUpdateAndReturnFlight() {
        Flight existingFlight = new Flight();
        existingFlight.setId(1L);
        Flight updatedDetails = new Flight();
        updatedDetails.setAirline("NewAir");

        when(flightRepository.findById(1)).thenReturn(Optional.of(existingFlight));
        when(flightRepository.save(any(Flight.class))).thenAnswer(inv -> inv.getArgument(0));

        Flight result = flightService.updateFlight(1, updatedDetails);

        assertEquals("NewAir", result.getAirline());
    }

    @Test
    void testUpdateFlight_WhenNotFound_ShouldThrowException() {
        when(flightRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> flightService.updateFlight(1, new Flight()));
    }

    @Test
    void testBookSeat_ShouldBookAvailableSeat() {
        Flight flight = new Flight();
        flight.setSeats(List.of(Seat.builder().seatNumber("1A").status(SeatStatus.AVAILABLE).build()));
        flight.setAvailableSeats(1);

        when(flightRepository.findById(1)).thenReturn(Optional.of(flight));
        when(flightRepository.save(any())).thenReturn(flight);

        FlightDetails result = flightService.bookSeat(1);

        assertEquals("1A", result.getSeatNumber());
        assertEquals(0, flight.getAvailableSeats());
    }

    @Test
    void testBookSeat_WhenNoSeatsAvailable_ShouldThrowException() {
        Flight flight = new Flight();
        flight.setSeats(List.of(Seat.builder().seatNumber("1A").status(SeatStatus.BOOKED).build()));

        when(flightRepository.findById(1)).thenReturn(Optional.of(flight));

        assertThrows(RuntimeException.class, () -> flightService.bookSeat(1));
    }

    @Test
    void testIsSeatAvailable_WhenAvailable_ShouldReturnTrue() {
        Flight flight = new Flight();
        flight.setSeats(List.of(Seat.builder().status(SeatStatus.AVAILABLE).build()));

        when(flightRepository.findById(1)).thenReturn(Optional.of(flight));

        assertTrue(flightService.isSeatAvailable(1));
    }
}
