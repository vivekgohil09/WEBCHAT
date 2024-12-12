package com.chat.chat.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    // Handles sending of messages to a specific room
    @MessageMapping("/sendMessage/{room}")
    @SendTo("/topic/room/{room}")
    public Message sendMessage(Message message, @DestinationVariable String room) {
        return message;  // Send message to room topic
    }

    // Handles typing indicator notifications
    @MessageMapping("/typing/{room}")
    @SendTo("/topic/typing/{room}")
    public Message sendTypingIndicator(Message message, @DestinationVariable String room) {
        return message;  // Broadcast typing indicator to the room
    }

    // Clear chat in a room
    @MessageMapping("/clearChat/{room}")
    public void clearChat(@DestinationVariable String room) {
        // Send a message to all users in the room to clear the chat window
        messagingTemplate.convertAndSend("/topic/clear/" + room, "clear");
    }

    // Inner class to handle message structure
    public static class Message {
        private String sender;
        private String content;
        private boolean typing;

        // Getters and setters
        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public boolean isTyping() {
            return typing;
        }

        public void setTyping(boolean typing) {
            this.typing = typing;
        }
    }
}

