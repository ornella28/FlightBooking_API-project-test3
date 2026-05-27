package se.lexicon.flightbooking_api.controller;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/api/assistant")
@CrossOrigin(origins = "http://localhost:5173")
public class AssistantController {

    private final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    private final List<ChatMessage> chatHistory = new ArrayList<>();



    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {

        String userMessage = request.message().toLowerCase();

        if (userMessage.contains("available flights") || userMessage.contains("search flights")) {
            String toolResult = searchAvailableFlights();
            return new ChatResponse(toolResult);
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

    private String searchAvailableFlights() {
        return "Available flights can be searched using the existing /api/flights/available endpoint.";
    }

    private String bookFlight(Long flightId, String passengerName, String passengerEmail) {
        return "Booking request received for flight " + flightId
                + ", passenger " + passengerName
                + ", email " + passengerEmail;
    }

    private String cancelBooking(Long flightId, String passengerEmail) {
        return "Cancel request received for flight " + flightId
                + ", email " + passengerEmail;
    }

    public record ChatRequest(String message) {}

    public record ChatMessage(String role, String content) {}

    public record ChatResponse(String reply) {}
}
