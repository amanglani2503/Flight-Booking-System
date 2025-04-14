package com.example.messaging_service.consumers;

import com.example.messaging_service.entity.MessagingDetails;
import com.example.messaging_service.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingNotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(BookingNotificationConsumer.class);

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = "booking_notification_queue")
    public void receiveMessage(MessagingDetails details) {
        logger.info("Received message for booking ID: {}", details.getBookingId());
        logger.debug("Message details: {}", details);

        try {
            if ("CONFIRMED".equals(details.getStatus())) {
                logger.info("Processing CONFIRMED booking notification");
                emailService.sendBookingEmail(details);
            } else {
                logger.info("Processing CANCELED booking notification");
                emailService.sendCancelEmail(details);
            }
        } catch (Exception e) {
            logger.error("Failed to process booking notification for booking ID: {}", details.getBookingId(), e);
        }
    }
}
