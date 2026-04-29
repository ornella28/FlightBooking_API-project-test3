package se.lexicon.flightbooking_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;

public record AvailableFlightDTO(
        Long id,
        @NotBlank(message = "Flight number is required")
        String flightNumber,
        @NotNull(message = "Departure time is required")
        LocalDateTime departureTime,
        @NotNull(message = "Arrival time is required")
        LocalDateTime arrivalTime,
        @NotBlank(message = "Destination is required")
        String destination,
        @Positive(message = "Price must be positive")
        Double price
) {}
