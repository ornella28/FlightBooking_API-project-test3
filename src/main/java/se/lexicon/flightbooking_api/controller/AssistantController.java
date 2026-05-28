package se.lexicon.flightbooking_api.controller;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.dto.AvailableFlightDTO;
import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;
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

        if (userMessage.startsWith("book flight")) {
            return handleBookingRequest(request.message());
        }

        if (userMessage.contains("available flights") || userMessage.contains("search flights")) {
            String toolResult = searchAvailableFlights();
            return new ChatResponse(toolResult);
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

        if (userMessage.contains("cancel booking")) {
            return new ChatResponse(
                    "I can help you cancel a booking. Please provide the flight ID and passenger email."
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

    private String searchAvailableFlights() {
        List<AvailableFlightDTO> flights = flightBookingService.findAvailableFlights();

        if (flights.isEmpty()) {
            return "There are currently no available flights.";
        }

        StringBuilder result = new StringBuilder("Here are the available flights:\n");

        for (AvailableFlightDTO flight : flights) {
            result.append("- Flight ID: ")
                    .append(flight.id())
                    .append(", Flight number: ")
                    .append(flight.flightNumber())
                    .append(", Destination: ")
                    .append(flight.destination())
                    .append(", Departure: ")
                    .append(flight.departureTime())
                    .append(", Arrival: ")
                    .append(flight.arrivalTime())
                    .append(", Price: ")
                    .append(flight.price())
                    .append(" kr\n");
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
        return "Cancel request received for flight " + flightId
                + ", email " + passengerEmail;
    }

    public record ChatRequest(String message) {}

    public record ChatMessage(String role, String content) {}

    public record ChatResponse(String reply) {}
}
