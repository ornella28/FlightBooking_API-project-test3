package se.lexicon.flightbooking_api.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import se.lexicon.flightbooking_api.service.ChatbotAssistant;

@RestController
@RequestMapping("/api/assistant")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class AssistantController {

    private final ChatbotAssistant chatbotAssistant;

    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
        String reply = chatbotAssistant.chat(request.chatId(), request.message());
        return new ChatResponse(reply);
    }

    public record ChatRequest(
            @NotBlank String chatId,
            @NotBlank String message
    ) {}

    public record ChatResponse(String reply) {}
}