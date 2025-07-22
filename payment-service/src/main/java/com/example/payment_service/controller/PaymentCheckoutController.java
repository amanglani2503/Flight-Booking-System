package com.example.payment_service.controller;

import com.example.payment_service.service.StripeService;
import com.example.payment_service.entity.PaymentRequest;
import com.example.payment_service.entity.StripeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pay")
public class PaymentCheckoutController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCheckoutController.class);

    private final StripeService stripeService;

    public PaymentCheckoutController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutProducts(@RequestBody PaymentRequest productRequest) {
        logger.info("Received checkout request: {}", productRequest);
        try {
            StripeResponse stripeResponse = stripeService.checkoutProducts(productRequest);
            logger.info("Stripe checkout session created successfully: {}", stripeResponse);
            return ResponseEntity.status(HttpStatus.OK).body(stripeResponse);
        } catch (Exception e) {
            logger.error("Error occurred while creating Stripe checkout session", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to create checkout session. Please try again later.");
        }
    }

    @GetMapping("/success")
    public ResponseEntity<String> handleSuccess(@RequestParam("bookingId") String bookingId,
                                                @RequestParam String seatNumber) {
        logger.info("Handling payment success for bookingId: {}, seatNumber: {}", bookingId, seatNumber);
        try {
            stripeService.handleSuccess(bookingId, seatNumber);
            logger.info("Payment handled successfully for bookingId: {}", bookingId);
            return ResponseEntity.ok("Payment done successfully");
        } catch (Exception e) {
            logger.error("Error occurred while handling payment success for bookingId: {}", bookingId, e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to complete payment processing.");
        }
    }
}
