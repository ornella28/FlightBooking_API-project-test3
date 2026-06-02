package se.lexicon.flightbooking_api.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import se.lexicon.flightbooking_api.dto.AvailableFlightDTO;
import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;
import se.lexicon.flightbooking_api.dto.FlightBookingDTO;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlightBookingToolService {

    private final FlightBookingService flightBookingService;

    @Tool(description = "Search and list all available flights. Always include flight ID, flight number, destination, departure time, arrival time and price.")
    public List<AvailableFlightDTO> searchAvailableFlights() {
        return flightBookingService.findAvailableFlights();
    }

    @Tool(description = "Find bookings by passenger email. Use this when the user asks to see bookings for an email address.")
    public List<FlightBookingDTO> findBookingsByEmail(String email) {
        return flightBookingService.findBookingsByEmail(email);
    }

    @Tool(description = "Book a flight using flight ID, passenger name and passenger email.")
    public FlightBookingDTO bookFlight(Long flightId, String passengerName, String passengerEmail) {
        BookFlightRequestDTO request = new BookFlightRequestDTO(passengerName, passengerEmail);
        return flightBookingService.bookFlight(flightId, request);
    }

    @Tool(description = "Cancel a booking using flight ID and passenger email.")
    public String cancelBooking(Long flightId, String passengerEmail) {
        flightBookingService.cancelFlight(flightId, passengerEmail);
        return "Booking cancelled successfully for flight ID " + flightId + ".";
    }
}
