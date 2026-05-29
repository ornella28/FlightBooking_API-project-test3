import {useEffect, useRef, useState} from "react";

type ChatMessage = {
    sender: "user" | "assistant";
    text: string;
};

function ChatbotPage() {
    const [messages, setMessages] = useState<ChatMessage[]>([]);
    const [input, setInput] = useState("");
    const [loading, setLoading] = useState(false);
    const messagesEndRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    }, [messages, loading]);

    function sendMessage(event: React.FormEvent) {
        event.preventDefault();

        if (!input.trim()) {
            return;
        }

        const userMessage: ChatMessage = {
            sender: "user",
            text: input,
        };

        setMessages((previousMessages) => [...previousMessages, userMessage]);
        setLoading(true);

        fetch("http://localhost:8080/api/assistant/chat", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({ message: input }),
        })
            .then((response) => {
                if (!response.ok) {
                    throw new Error("Assistant request failed");
                }
                return response.json();
            })
            .then((data) => {
                const assistantMessage: ChatMessage = {
                    sender: "assistant",
                    text: data.reply,
                };

                setMessages((previousMessages) => [
                    ...previousMessages,
                    assistantMessage,
                ]);

                setInput("");
            })
            .catch(() => {
                const errorMessage: ChatMessage = {
                    sender: "assistant",
                    text: "Something went wrong. Please try again.",
                };

                setMessages((previousMessages) => [
                    ...previousMessages,
                    errorMessage,
                ]);
            })
            .finally(() => {
                setLoading(false);
            });
    }

    return (
        <div className="chat-page">
            <h2 className="page-title">Flight Assistant</h2>
            <div className="chat-tips">
                <p>Try asking:</p>
                <span>Show me available flights</span>
                <span>Show bookings for anna@email.com</span>
                <span>Cancel booking 3 for anna@email.com</span>
                <span>Book flight 3 for Anna Svensson, anna@email.com</span>
            </div>

            <div className="chat-container">
                {messages.length === 0 && (
                    <p className="chat-placeholder">
                        Try: “Show me available flights”
                    </p>
                )}

                {messages.map((message, index) => (
                    <div
                        key={index}
                        className={
                            message.sender === "user"
                                ? "chat-message user-message"
                                : "chat-message assistant-message"
                        }
                    >
                        {message.text}
                    </div>
                ))}

                {loading && (
                    <div className="chat-message assistant-message">
                        Assistant is typing...
                    </div>
                )}
                <div ref={messagesEndRef}></div>
            </div>

            <form className="chat-form" onSubmit={sendMessage}>
                <input
                    type="text"
                    placeholder="Ask the flight assistant..."
                    value={input}
                    onChange={(event) => setInput(event.target.value)}
                />

                <button type="submit">Send</button>
            </form>
        </div>
    );
}

export default ChatbotPage;