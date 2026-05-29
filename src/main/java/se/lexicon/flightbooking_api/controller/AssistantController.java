package se.lexicon.flightbooking_api.controller;

import com.openai.client.OpenAIClient;import com.openai.client.okhttp.OpenAIOkHttpClient;import com.openai.models.ChatModel;import com.openai.models.chat.completions.ChatCompletion;import com.openai.models.chat.completions.ChatCompletionCreateParams;import org.springframework.web.bind.annotation.*;import se.lexicon.flightbooking_api.dto.AvailableFlightDTO;import se.lexicon.flightbooking_api.dto.BookFlightRequestDTO;import se.lexicon.flightbooking_api.dto.FlightBookingDTO;import se.lexicon.flightbooking_api.service.FlightBookingService;

import java.util.ArrayList;import java.util.List;

@RestController@RequestMapping("/api/assistant")@CrossOrigin(origins = "http://localhost:5173")public class AssistantController {

    private final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    private final List<ChatMessage> chatHistory = new ArrayList<>();

    private final FlightBookingService flightBookingService;

    public AssistantController(FlightBookingService flightBookingService) {
        this.flightBookingService = flightBookingService;
    }



    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String userMessage = request.message().toLowerCase();

        String intent = detectIntent(request.message());

        if (intent.equals("SEARCH_AVAILABLE_FLIGHTS")) {
            String toolResult = searchAvailableFlights();
            String reply = formatToolResultForUser(request.message(), toolResult);
            return new ChatResponse(reply);
        }

        if (intent.equals("FIND_BOOKINGS_BY_EMAIL")) {
            ChatResponse toolResponse = handleFindBookingsByEmail(request.message());
            String reply = formatToolResultForUser(request.message(), toolResponse.reply());
            return new ChatResponse(reply);
        }

        if (intent.equals("BOOK_FLIGHT")) {
            if (request.message().contains("@")) {
                return handleBookingRequest(request.message());
            }

            return new ChatResponse("""
        I can help you book a flight.

        Please provide:
        - Flight ID
        - Passenger name
        - Passenger email

        Example:
        Book flight 3 for Anna Svensson, anna@email.com
        """);
        }

        if (intent.equals("CANCEL_BOOKING")) {
            ChatResponse toolResponse = handleCancelRequest(request.message());
            String reply = formatToolResultForUser(request.message(), toolResponse.reply());
            return new ChatResponse(reply);
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

    private String detectIntent(String message) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)
                .addSystemMessage("""
                You classify flight assistant requests.

                Return only one of these exact labels:
                SEARCH_AVAILABLE_FLIGHTS
                FIND_BOOKINGS_BY_EMAIL
                BOOK_FLIGHT
                CANCEL_BOOKING
                GENERAL_CHAT

                Understand any language.
                Do not explain.
                """)
                .addUserMessage(message)
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        return completion.choices()
                .getFirst()
                .message()
                .content()
                .orElse("GENERAL_CHAT")
                .trim();
    }

    private String formatToolResultForUser(String userMessage, String toolResult) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)
                .addSystemMessage("""
                You are a helpful flight reservation assistant.

                Detect the language of the user message.
                Reply ONLY in the same language as the user.
                Do not mix languages.

                Format the answer using this style:
                - Short intro sentence
                - One separated block per flight or booking
                - Use the ✈️ emoji for each flight/booking
                - Keep line breaks
                - Keep separator lines like ------------------------
                - Keep all IDs, emails, passenger names, prices, dates, destinations and statuses unchanged
                - Do not invent information
                - Do not write long paragraphs

                Translate only the labels, for example:
                Flight ID, Flight number, Destination, Departure, Arrival, Price, Status, Passenger, Email.
                """)
                .addUserMessage("""
                User message:
                %s

                Tool result:
                %s
                """.formatted(userMessage, toolResult))
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        return completion.choices()
                .getFirst()
                .message()
                .content()
                .orElse(toolResult);
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