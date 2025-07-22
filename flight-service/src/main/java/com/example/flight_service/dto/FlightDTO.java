package com.example.flight_service.dto;

import com.example.flight_service.entity.FlightScheduleType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightDTO {
    // Basic flight info
    @NotBlank(message = "Flight code is required")
    private String flightCode;

    @NotBlank(message = "Carrier name is required")
    private String carrierName;

    @NotBlank(message = "Departure city is required")
    private String departureCity;

    @NotBlank(message = "Arrival city is required")
    private String arrivalCity;

    @NotNull(message = "Time of departure is required")
    private LocalTime timeOfDeparture;

    @NotNull(message = "Time of arrival is required")
    private LocalTime timeOfArrival;

    @Min(value = 1, message = "Seat capacity must be at least 1")
    private int seatCapacity;

    @PositiveOrZero(message = "Seats available must be 0 or more")
    private int seatsAvailable;

    @DecimalMin(value = "0.0", inclusive = true, message = "Ticket price must be 0 or more")
    private double ticketPrice;

    // Schedule details
    @NotNull(message = "Schedule type is required")
    private FlightScheduleType scheduleType;

    // Applicable for DAILY, WEEKLY, MONTHLY
    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    // For WEEKLY (1 = Monday, ..., 7 = Sunday)
    private List<@Min(1) @Max(7) Integer> runningDaysOfWeek;

    // For MONTHLY (1–31)
    private List<@Min(1) @Max(31) Integer> daysOfMonth;

    // For CUSTOM_DATES
    private List<@NotNull LocalDate> specificDates;
}
