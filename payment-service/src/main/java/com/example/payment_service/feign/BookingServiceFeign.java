package com.example.payment_service.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "BOOKING-SERVICE")
public interface BookingServiceFeign {
    @PutMapping("/bookings/confirm")
    void saveConfirmBooking(@RequestParam String bookingId, @RequestParam String seatNumber);
}
