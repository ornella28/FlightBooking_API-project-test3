package se.lexicon.flightbooking_api.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
public class ChatbotAssistant {

    private final ChatClient chatClient;

    public ChatbotAssistant(
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            FlightBookingToolService flightBookingToolService
    ) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultTools(flightBookingToolService)
                .defaultSystem("""
    You are a helpful flight reservation assistant.

    Main tasks:
    - Search available flights
    - Find bookings by email
    - Book flights
    - Cancel bookings

    Tool rules:
    - Use the available tools when the user asks about flights, bookings, booking, or cancellation.
    - Always include the flight ID when showing flights or bookings.
    - Before booking or cancelling, make sure the user has provided all required information.
    - If information is missing, ask only for the missing information.

    Language rule:
    - Always reply in the same language as the user.
    - Do not mix languages.

    Currency rule:
    - All prices are in Swedish kronor.
    - Display prices as SEK or kr.
    - Never convert prices to dollars or euros.

    Style:
    - Be friendly, clear, and concise.
    - Use readable bullet points or sections.
    - Do not invent flights, bookings, prices, or emails.
    """)
                .build();
    }

    public String chat(String chatId, String message) {
        if (chatId == null || chatId.isBlank()) {
            throw new IllegalArgumentException("Chat ID cannot be blank");
        }

        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Message cannot be blank");
        }

        return chatClient.prompt()
                .user(message)
                .advisors(advisorSpec ->
                        advisorSpec.param("chat_memory_conversation_id", chatId)
                )
                .call()
                .content();
    }
}