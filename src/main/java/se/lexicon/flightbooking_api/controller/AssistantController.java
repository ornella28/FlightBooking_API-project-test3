package se.lexicon.flightbooking_api.controller;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.dto.AvailableFlightDTO;
import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;
import se.lexicon.flightbooking_api.dto.FlightBookingDTO;
import se.lexicon.flightbooking_api.service.FlightBookingService;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/assistant")
@CrossOrigin(origins = "http://localhost:5173")
public class AssistantController {

    private final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    private final List<ChatMessage> chatHistory = new ArrayList<>();

    private final FlightBookingService flightBookingService;

    public AssistantController(FlightBookingService flightBookingService) {
        this.flightBookingService = flightBookingService;
    }



    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String userMessage = request.message().toLowerCase();

        if (userMessage.contains("cancel")) {
            return handleCancelRequest(request.message());
        }

        if (userMessage.startsWith("book flight")) {
            return handleBookingRequest(request.message());
        }

        if (userMessage.contains("available flights") || userMessage.contains("search flights")) {
            String toolResult = searchAvailableFlights();
            return new ChatResponse(toolResult);
        }

        if (userMessage.contains("booking") && userMessage.contains("@")) {
            return handleFindBookingsByEmail(request.message());
        }



        if (userMessage.contains("book flight") || userMessage.contains("book a flight")) {
            return new ChatResponse(
                    """
                    I can help you book a flight.
        
                    Please provide:
                    - Flight ID
                    - Passenger name
                    - Passenger email
        
                    Example:
                    Book flight 3 for Anna Svensson, anna@email.com
                    """
            );
        }

        chatHistory.add(new ChatMessage("user", request.message()));

        if (chatHistory.size() > 10) {
            chatHistory.removeFirst();
        }

        ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)
                .addSystemMessage("""
                    You are a helpful flight reservation assistant.
                    Help users search flights, book flights, and cancel bookings.
                    Be clear, friendly, and ask for missing information when needed.
                    Keep answers short and practical.
                    """);

        for (ChatMessage message : chatHistory) {
            if (message.role().equals("user")) {
                builder.addUserMessage(message.content());
            } else if (message.role().equals("assistant")) {
                builder.addAssistantMessage(message.content());
            }
        }

        ChatCompletionCreateParams params = builder.build();

        ChatCompletion completion = client.chat().completions().create(params);

        String reply = completion.choices()
                .getFirst()
                .message()
                .content()
                .orElse("Sorry, I could not generate a response.");

        chatHistory.add(new ChatMessage("assistant", reply));

        if (chatHistory.size() > 10) {
            chatHistory.removeFirst();
        }

        return new ChatResponse(reply);
    }

    private ChatResponse handleBookingRequest(String message) {
        try {
            String[] parts = message.split("for");

            Long flightId = Long.parseLong(
                    parts[0].replaceAll("[^0-9]", "")
            );

            String passengerInfo = parts[1].trim();

            String[] passengerParts = passengerInfo.split(",");

            String passengerName = passengerParts[0].trim();
            String passengerEmail = passengerParts[1].trim();

            String result = bookFlight(flightId, passengerName, passengerEmail);

            return new ChatResponse(result);

        } catch (Exception e) {
            return new ChatResponse(
                    """
                    I can help you book the flight, but I need the information in this format:
    
                    Book flight 3 for Anna Svensson, anna@email.com
                    """
            );
        }
    }

    private ChatResponse handleCancelRequest(String message) {
        try {
            String[] parts = message.split("for");

            Long flightId = Long.parseLong(
                    parts[0].replaceAll("[^0-9]", "")
            );

            String passengerEmail = parts[1].trim();

            String result = cancelBooking(flightId, passengerEmail);

            return new ChatResponse(result);

        } catch (Exception e) {
            return new ChatResponse(
                    """
                    I can help you cancel a booking, but I need the information in this format:
    
                    Cancel booking 3 for anna@email.com
                    """
            );
        }
    }

    private String searchAvailableFlights() {
        List<AvailableFlightDTO> flights = flightBookingService.findAvailableFlights();

        if (flights.isEmpty()) {
            return "There are currently no available flights.";
        }

        StringBuilder result = new StringBuilder("Here are the available flights:\n");

        for (AvailableFlightDTO flight : flights) {
            result.append("""
        
        ✈️ Flight ID: %d
        Flight number: %s
        Destination: %s
        Departure: %s
        Arrival: %s
        Price: %.2f kr
        ------------------------
        """.formatted(
                    flight.id(),
                    flight.flightNumber(),
                    flight.destination(),
                    flight.departureTime(),
                    flight.arrivalTime(),
                    flight.price()
            ));
        }

        return result.toString();
    }

    private String bookFlight(Long flightId, String passengerName, String passengerEmail) {
        try {
            BookFlightRequestDTO request = new BookFlightRequestDTO(
                    passengerName,
                    passengerEmail
            );

            var booking = flightBookingService.bookFlight(flightId, request);

            return "Flight booked successfully for "
                    + booking.passengerName()
                    + ". Flight number: "
                    + booking.flightNumber()
                    + ", destination: "
                    + booking.destination()
                    + ".";
        } catch (Exception e) {
            return "Sorry, I could not complete the booking. " + e.getMessage();
        }
    }

    private String cancelBooking(Long flightId, String passengerEmail) {
        try {
            flightBookingService.cancelFlight(flightId, passengerEmail);

            return "Booking cancelled successfully for flight ID "
                    + flightId
                    + " and email "
                    + passengerEmail
                    + ".";
        } catch (Exception e) {
            return "Sorry, I could not cancel the booking. " + e.getMessage();
        }
    }

    private String findBookingsByEmail(String email) {
        List<FlightBookingDTO> bookings = flightBookingService.findBookingsByEmail(email);

        if (bookings.isEmpty()) {
            return "No bookings were found for " + email + ".";
        }

        StringBuilder result = new StringBuilder("Bookings found for " + email + ":\n");

        for (FlightBookingDTO booking : bookings) {
            result.append("""
            
            ✈️ Flight ID: %d
            Passenger: %s
            Email: %s
            Flight number: %s
            Destination: %s
            Departure: %s
            Arrival: %s
            Status: %s
            ------------------------
            """.formatted(
                    booking.id(),
                    booking.passengerName(),
                    booking.passengerEmail(),
                    booking.flightNumber(),
                    booking.destination(),
                    booking.departureTime(),
                    booking.arrivalTime(),
                    booking.status()
            ));
        }

        return result.toString();
    }

    private String extractEmail(String message) {
        String[] words = message.split("\\s+");

        for (String word : words) {
            if (word.contains("@")) {
                return word.replace(",", "").trim();
            }
        }

        throw new IllegalArgumentException("No email found.");
    }

    private ChatResponse handleFindBookingsByEmail(String message) {
        try {
            String email = extractEmail(message);

            String result = findBookingsByEmail(email);

            return new ChatResponse(result);

        } catch (Exception e) {
            return new ChatResponse(
                    """
                    I can help you find bookings by email.
    
                    Please write your request like this:
    
                    Show bookings for anna@email.com
                    """
            );
        }
    }

    public record ChatRequest(String message) {}

    public record ChatMessage(String role, String content) {}

    public record ChatResponse(String reply) {}
}
