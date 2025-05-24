package com.example.flight_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "flights")
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Airline name cannot be blank")
    private String airline;

    @NotBlank(message = "Departure location cannot be blank")
    private String departure;

    @NotBlank(message = "Destination cannot be blank")
    private String destination;

    @NotNull(message = "Departure time must be specified")
    private LocalDateTime departureTime;

    @NotNull(message = "Arrival time must be specified")
    private LocalDateTime arrivalTime;

    @PositiveOrZero(message = "Available seats must be 0 or more")
    private int availableSeats;

    @Positive(message = "Price must be greater than zero")
    private double price;

    @Min(value = 6, message = "Total seats must be at least 6")
    private int totalSeats;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Seat> seats = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        this.availableSeats = this.totalSeats;
    }

    @PostPersist
    public void initializeSeats() {
        if ((seats == null || seats.isEmpty()) && totalSeats > 0) {
            List<Seat> newSeats = new ArrayList<>();
            int rows = (int) Math.ceil((double) totalSeats / 6);
            for (int row = 1; row <= rows; row++) {
                for (char col = 'A'; col <= 'F'; col++) {
                    int seatIndex = (row - 1) * 6 + (col - 'A' + 1);
                    if (seatIndex > totalSeats) break;

                    Seat seat = Seat.builder()
                            .seatNumber(row + String.valueOf(col))
                            .status(SeatStatus.AVAILABLE)
                            .flight(this)
                            .build();
                    newSeats.add(seat);
                }
            }
            this.seats = newSeats;
        }
    }
}
