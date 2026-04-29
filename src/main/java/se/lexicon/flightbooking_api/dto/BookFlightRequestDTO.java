package se.lexicon.flightbooking_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BookFlightRequestDTO(
        @NotBlank(message = "Passenger name is required")
        @Size(min = 2, max = 100, message = "Passenger name must be between 2 and 100 characters")
        String passengerName,

        @NotBlank(message = "Passenger email is required")
        @Email(message = "Invalid email format")
        String passengerEmail
) {}
