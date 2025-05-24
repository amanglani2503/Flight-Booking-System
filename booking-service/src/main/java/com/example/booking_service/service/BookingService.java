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

    public StripeResponse bookFlight(Booking booking) {
        String token = extractTokenFromRequest();
        String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

        boolean available = flightServiceFeign.isSeatAvailable(booking.getFlightId(), authHeader);
        // Replace this in bookFlight
        if (!available) {
            throw new SeatUnavailableException("Seat is not available!");
        }


        BookingResponse pendingBooking = new BookingResponse(
                booking.getEmailId(),
                booking.getBookingId(),
                booking.getPassengerName(),
                booking.getFlightId(),
                booking.getBookingDate(),
                "PENDING",
                null
        );
        bookingRepository.save(pendingBooking);

        try {
            FlightDetails flightDetails = flightServiceFeign.bookSeat(booking.getFlightId(), authHeader);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(flightDetails.getTotalAmountPaid());
            paymentRequest.setCurrency("usd");
            paymentRequest.setFlightId(booking.getFlightId());
            paymentRequest.setBookingId(String.valueOf(booking.getBookingId()));
            paymentRequest.setUserId(booking.getEmailId());

            StripeResponse paymentResponse = paymentServiceFeign.makePayment(paymentRequest, authHeader);
            // Replace this in case of payment failure
            if (!"SUCCESS".equalsIgnoreCase(paymentResponse.getStatus())) {
                throw new PaymentFailedException("Payment failed: " + paymentResponse.getMessage());
            }


            BookingResponse confirmedBooking = new BookingResponse(
                    booking.getEmailId(),
                    booking.getBookingId(),
                    booking.getPassengerName(),
                    booking.getFlightId(),
                    booking.getBookingDate(),
                    "CONFIRMED",
                    flightDetails.getSeatNumber()
            );
            bookingRepository.save(confirmedBooking);

            MessagingDetails messagingDetails = new MessagingDetails(
                    confirmedBooking.getEmailId(),
                    confirmedBooking.getPassengerName(),
                    confirmedBooking.getBookingId(),
                    confirmedBooking.getFlightId(),
                    flightDetails.getDepartureAirport(),
                    flightDetails.getArrivalAirport(),
                    flightDetails.getDepartureTime(),
                    flightDetails.getArrivalTime(),
                    flightDetails.getSeatNumber(),
                    flightDetails.getTotalAmountPaid(),
                    "CONFIRMED"
            );
            messagingServiceFeign.sendMessage(messagingDetails, authHeader);

            return paymentResponse;

        } catch (Exception ex) {
            BookingResponse failedBooking = new BookingResponse(
                    booking.getEmailId(),
                    booking.getBookingId(),
                    booking.getPassengerName(),
                    booking.getFlightId(),
                    booking.getBookingDate(),
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
            existingBooking.setStatus("CANCELED");
            bookingRepository.save(existingBooking);

            String token = extractTokenFromRequest();
            String authHeader = token.startsWith("Bearer ") ? token : "Bearer " + token;

            flightServiceFeign.cancelSeat(existingBooking.getFlightId(), existingBooking.getSeatNumber(), authHeader);
            FlightDetails flightDetails = flightServiceFeign.getFlightDetails(existingBooking.getFlightId(), authHeader);

            MessagingDetails messagingDetails = new MessagingDetails(
                    existingBooking.getEmailId(),
                    existingBooking.getPassengerName(),
                    existingBooking.getBookingId(),
                    existingBooking.getFlightId(),
                    flightDetails.getDepartureAirport(),
                    flightDetails.getArrivalAirport(),
                    flightDetails.getDepartureTime(),
                    flightDetails.getArrivalTime(),
                    existingBooking.getSeatNumber(),
                    flightDetails.getTotalAmountPaid(),
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

    public List<BookingResponse> getBookingsByUser(String emailId) {
        return bookingRepository.findByEmailId(emailId);
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
        // Replace this in extractTokenFromRequest
        throw new AuthorizationException("Authorization header not found");

    }
}