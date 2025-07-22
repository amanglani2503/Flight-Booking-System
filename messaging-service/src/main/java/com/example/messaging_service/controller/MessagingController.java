package com.example.messaging_service.controller;

import com.example.messaging_service.config.RabbitMQConfig;
import com.example.messaging_service.entity.MessagingDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/message")
public class MessagingController {

    private static final Logger logger = LoggerFactory.getLogger(MessagingController.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @PostMapping("/notify")
    public ResponseEntity<String> sendMessage(@RequestBody MessagingDetails details) {
        logger.info("Received message request for booking ID: {}", details.getBookingId());
        logger.debug("Messaging details: {}", details);

        try {
            rabbitTemplate.convertAndSend(rabbitMQConfig.getQueueName(), details);
            logger.info("Message successfully sent to RabbitMQ queue '{}'", rabbitMQConfig.getQueueName());
            return ResponseEntity.ok("Message sent to RabbitMQ");
        } catch (Exception e) {
            logger.error("Error while sending message to RabbitMQ queue '{}'", rabbitMQConfig.getQueueName(), e);
            return ResponseEntity.status(500).body("Failed to send message to RabbitMQ");
        }
    }
}
