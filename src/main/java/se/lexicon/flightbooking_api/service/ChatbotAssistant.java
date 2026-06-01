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
            ChatMemory chatMemory
    ) {
        this.chatClient = chatClientBuilder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .defaultSystem("""
                        You are a helpful flight reservation assistant.

                        You help users:
                        - search available flights
                        - find bookings by email
                        - book flights
                        - cancel bookings

                        Important language rule:
                        - Always reply in the same language the user uses.
                        - Do not mix languages.
                        - If the user writes in English, reply in English.
                        - If the user writes in Swedish, reply in Swedish.
                        - If the user writes in French, reply in French.

                        Style:
                        - Be clear, friendly, and practical.
                        - Ask for missing information when needed.
                        - Keep answers short and easy to read.
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