package com.example.messaging_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Value("${rabbitmq.queue.name}")
    private String queueName;  // Injects queue name from properties

    // Defines a durable RabbitMQ queue bean
    @Bean
    public Queue queue() {
        logger.info("Creating RabbitMQ queue with name: {}", queueName);
        return new Queue(queueName, true);
    }

    // Configures JSON message converter
    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        logger.debug("Creating Jackson2JsonMessageConverter for RabbitMQ");
        return new Jackson2JsonMessageConverter();
    }

    // Configures RabbitTemplate with custom message converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        logger.info("Configuring RabbitTemplate with JSON message converter");
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    public String getQueueName() {
        logger.debug("Retrieving configured queue name: {}", queueName);
        return queueName;
    }
}
