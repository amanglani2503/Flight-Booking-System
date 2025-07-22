package com.example.booking_service.service;

import com.example.booking_service.customexceptions.AuthorizationException;
import com.example.booking_service.customexceptions.BookingNotFoundException;
import com.example.booking_service.customexceptions.PaymentFailedException;
import com.example.booking_service.customexceptions.SeatUnavailableException;
import com.example.booking_service.entity.*;
import com.example.booking_service.feign.FlightServiceFeign;
import com.example.booking_service.feign.MessagingServiceFeign;
import com.example.booking_service.feign.PaymentServiceFeign;
import com.example.booking_service.repository.BookingRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FlightServiceFeign flightServiceFeign;

    @Autowired
    private MessagingServiceFeign messagingServiceFeign;

    @Autowired
    private PaymentServiceFeign paymentServiceFeign;

    @Transactional
    public StripeResponse bookFlight(Booking booking) {
        String token = extractTokenFromRequest();
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        System.out.println(booking.getTravelDate());
        boolean available = flightServiceFeign.isSeatAvailable(booking.getFlightCode(), booking.getTravelDate(), authHeader);
        if (!available) {
            throw new SeatUnavailableException("Seat is not available!");
        }

        BookingResponse pendingBooking = new BookingResponse(
                booking.getEmail(),
                null,
                booking.getPassengerName(),
                booking.getFlightCode(),
                booking.getDateOfBooking(),
                booking.getTravelDate(),
                "PENDING",
                null
        );
        bookingRepository.save(pendingBooking);

        try {
            FlightDetails flightDetails = flightServiceFeign.bookSeat(booking.getFlightCode(), booking.getTravelDate(), authHeader);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setBookingId(String.valueOf(pendingBooking.getId()));
            paymentRequest.setCurrency("usd");
            paymentRequest.setSeatNumber(flightDetails.getSeatNumber());
            paymentRequest.setAmount(flightDetails.getAmountPaid());
            paymentRequest.setFlightNumber(booking.getFlightCode());
            paymentRequest.setUserId(booking.getEmail());

            StripeResponse paymentResponse = paymentServiceFeign.makePayment(paymentRequest, authHeader);

            if (!"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
                throw new PaymentFailedException("Payment failed: " + paymentResponse.getMessage());
            }

            MessagingDetails messagingDetails = new MessagingDetails(
                    booking.getEmail(),
                    booking.getPassengerName(),
                    pendingBooking.getId(),
                    booking.getFlightCode(),
                    flightDetails.getDepartureLocation(),
                    flightDetails.getArrivalLocation(),
                    flightDetails.getTimeOfDeparture(),
                    flightDetails.getTimeOfArrival(),
                    flightDetails.getTravelDate(),
                    flightDetails.getDateOfBooking(),
                    flightDetails.getSeatNumber(),
                    flightDetails.getAmountPaid(),
                    "CONFIRMED"
            );
            System.out.println(messagingDetails);
            messagingServiceFeign.sendMessage(messagingDetails, authHeader);

            return paymentResponse;

        } catch (Exception ex) {
            BookingResponse failedBooking = new BookingResponse(
                    booking.getEmail(),
                    null,
                    booking.getPassengerName(),
                    booking.getFlightCode(),
                    booking.getDateOfBooking(),
                    booking.getTravelDate(),
                    "FAILED",
                    null
            );
            bookingRepository.save(failedBooking);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking failed: " + ex.getMessage());
        }
    }

    public void cancelBooking(Long bookingId) {
        Optional<BookingResponse> booking = bookingRepository.findById(bookingId);
        if (booking.isPresent()) {
            BookingResponse existingBooking = booking.get();
            existingBooking.setBookingStatus("CANCELED");
            bookingRepository.save(existingBooking);

            String token = extractTokenFromRequest();
            String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

            flightServiceFeign.cancelSeat(existingBooking.getFlightNumber(), existingBooking.getJourneyDate(), existingBooking.getSeatNumber(), authHeader);
            FlightDetails flightDetails = flightServiceFeign.getFlightDetails(existingBooking.getFlightNumber(), existingBooking.getJourneyDate(), authHeader);

            MessagingDetails messagingDetails = new MessagingDetails(
                    existingBooking.getEmail(),
                    existingBooking.getPassengerName(),
                    existingBooking.getId(),
                    existingBooking.getFlightNumber(),
                    flightDetails.getDepartureLocation(),
                    flightDetails.getArrivalLocation(),
                    flightDetails.getTimeOfDeparture(),
                    flightDetails.getTimeOfArrival(),
                    flightDetails.getTravelDate(),
                    flightDetails.getDateOfBooking(),
                    existingBooking.getSeatNumber(),
                    flightDetails.getAmountPaid(),
                    "CANCELED"
            );
            messagingServiceFeign.sendMessage(messagingDetails, authHeader);
        } else {
            throw new BookingNotFoundException("Booking not found with given Id");
        }
    }

    public BookingResponse getBookingDetails(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + bookingId));
    }

    public List<BookingResponse> getBookingsByUser(String email) {
        return bookingRepository.findByEmail(email);
    }

    private String extractTokenFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String token = request.getHeader("Authorization");
            if (token != null) {
                return token;
            }
        }
        throw new AuthorizationException("Authorization header not found");
    }

    public void saveConfirmedBooking(Long bookingId, String seatNumber) {
        BookingResponse bookingResponse = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        bookingResponse.setSeatNumber(seatNumber);
        bookingResponse.setBookingStatus("CONFIRMED");
        bookingRepository.save(bookingResponse);
    }
}
