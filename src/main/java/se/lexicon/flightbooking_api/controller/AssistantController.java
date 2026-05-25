package se.lexicon.flightbooking_api.controller;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assistant")
@CrossOrigin(origins = "http://localhost:5173")
public class AssistantController {

    private final OpenAIClient client = OpenAIOkHttpClient.fromEnv();

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)
                .addSystemMessage("""
                        You are a helpful flight reservation assistant.
                        Help users search flights, book flights, and cancel bookings.
                        Be clear, friendly, and ask for missing information when needed.
                        Keep answers short and practical.
                        """)
                .addUserMessage(request.message())
                .build();

        ChatCompletion completion = client.chat().completions().create(params);

        String reply = completion.choices()
                .getFirst()
                .message()
                .content()
                .orElse("Sorry, I could not generate a response.");

        return new ChatResponse(reply);
    }

    public record ChatRequest(String message) {}

    public record ChatResponse(String reply) {}
}
