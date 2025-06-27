package com.chat.chat.model;

public class ChatMessage {

    // Enum to define different types of chat messages
    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        USER_LIST_UPDATE // New message type for updating the list of online users
    }

    private String content; // Can be chat message, or JSON string of usernames for USER_LIST_UPDATE
    private boolean typing; // Indicates if sender is typing
    private String sender;  // Username of the sender
    private MessageType type; // Type of message (CHAT, JOIN, LEAVE, USER_LIST_UPDATE)

    public ChatMessage() {
        // Default constructor
    }

    public ChatMessage(String content, boolean typing, String sender, MessageType type) {
        this.content = content;
        this.typing = typing;
        this.sender = sender;
        this.type = type;
    }

    // Getters and Setters for all fields

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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }
}
