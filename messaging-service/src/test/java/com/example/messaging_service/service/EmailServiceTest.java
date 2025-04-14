package com.example.messaging_service.service;

import com.example.messaging_service.entity.MessagingDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class EmailServiceTest {

    @InjectMocks
    private EmailService emailService;

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> mailCaptor;

    private MessagingDetails details;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        details = new MessagingDetails();
        details.setRecipientEmail("john.doe@example.com");
        details.setPassengerName("John Doe");
        details.setBookingId("BOOK123");
        details.setFlightId("FL123");
        details.setDepartureAirport("LAX");
        details.setArrivalAirport("JFK");
        details.setDepartureTime(LocalDateTime.of(2025, 4, 15, 10, 30));
        details.setArrivalTime(LocalDateTime.of(2025, 4, 15, 16, 45));
        details.setSeatNumber("12A");
        details.setTotalAmountPaid(299.99);
        details.setStatus("CONFIRMED");
    }

    @Test
    void testSendBookingEmail() {
        emailService.sendBookingEmail(details);

        verify(mailSender, times(1)).send(mailCaptor.capture());
        SimpleMailMessage sentMessage = mailCaptor.getValue();

        assert sentMessage.getTo()[0].equals("john.doe@example.com");
        assert sentMessage.getSubject().contains("Booking Confirmation");
        assert sentMessage.getText().contains("Your booking (ID: BOOK123)");
    }

    @Test
    void testSendCancelEmail() {
        emailService.sendCancelEmail(details);

        verify(mailSender, times(1)).send(mailCaptor.capture());
        SimpleMailMessage sentMessage = mailCaptor.getValue();

        assert sentMessage.getTo()[0].equals("john.doe@example.com");
        assert sentMessage.getSubject().contains("Booking Cancellation");
        assert sentMessage.getText().contains("We regret to inform you that your booking");
    }
}
