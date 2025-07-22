package com.example.messaging_service.service;

import com.example.messaging_service.entity.MessagingDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingEmail(MessagingDetails details) {
        logger.info("Preparing booking confirmation email for booking ID: {}", details.getBookingId());
        logger.debug("Booking email payload: {}", details);

        try {
            SimpleMailMessage message = createMessage(details, true);
            mailSender.send(message);
            logger.info("Booking confirmation email sent to {}", details.getRecipientEmail());
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email to {}", details.getRecipientEmail(), e);
        }
    }

    public void sendCancelEmail(MessagingDetails details) {
        logger.info("Preparing booking cancellation email for booking ID: {}", details.getBookingId());
        logger.debug("Cancellation email payload: {}", details);

        try {
            SimpleMailMessage message = createMessage(details, false);
            mailSender.send(message);
            logger.info("Cancellation email sent to {}", details.getRecipientEmail());
        } catch (Exception e) {
            logger.error("Failed to send cancellation email to {}", details.getRecipientEmail(), e);
        }
    }

    private SimpleMailMessage createMessage(MessagingDetails details, boolean isConfirmed) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(details.getRecipientEmail());

        if (isConfirmed) {
            message.setSubject("Booking Confirmation - " + details.getBookingId());
            message.setText(
                    "Dear " + details.getPassengerName() + ",\n\n"
                            + "Your booking (ID: " + details.getBookingId() + ") for Flight " + details.getFlightNumber()
                            + " from " + details.getDepartureAirport() + " to " + details.getArrivalAirport()
                            + " has been confirmed.\n\n"
                            + "Seat Number: " + details.getSeatNumber() + "\n"
                            + "Departure: " + details.getDepartureTime() + "\n"
                            + "Arrival: " + details.getArrivalTime() + "\n"
                            + "Total Amount Paid: $" + details.getTotalAmountPaid() + "\n\n"
                            + "Thank you for choosing our service!"
            );
        } else {
            message.setSubject("Booking Cancellation - " + details.getBookingId());
            message.setText(
                    "Dear " + details.getPassengerName() + ",\n\n"
                            + "We regret to inform you that your booking (ID: " + details.getBookingId() + ") for Flight " + details.getFlightNumber()
                            + " from " + details.getDepartureAirport() + " to " + details.getArrivalAirport()
                            + " has been canceled.\n\n"
                            + "Seat Number: " + details.getSeatNumber() + "\n"
                            + "Originally scheduled Departure: " + details.getDepartureTime() + "\n"
                            + "Originally scheduled Arrival: " + details.getArrivalTime() + "\n"
                            + "Refund Amount (if applicable): $" + details.getTotalAmountPaid() + "\n\n"
                            + "If you have any questions or need further assistance, feel free to contact us.\n\n"
                            + "We hope to serve you again soon!"
            );
        }

        logger.debug("Email message created: Subject = '{}'", message.getSubject());
        return message;
    }
}
