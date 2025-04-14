package com.example.flight_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
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

    private String airline;
    private String departure;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int availableSeats;
    private double price;

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
        if (seats == null || seats.isEmpty() && totalSeats > 0) {
            List<Seat> newSeats = new ArrayList<>();
            int rows = (int) Math.ceil((double) totalSeats / 6); // Calculate rows dynamically
            for (int row = 1; row <= rows; row++) {
                for (char col = 'A'; col <= 'F'; col++) {
                    int seatIndex = (row - 1) * 6 + (col - 'A' + 1);
                    if (seatIndex > totalSeats) break; // Stop if we exceed the total seats

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

