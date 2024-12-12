package com.chat.chat.model;

public class ChatMessage {

    private String content;
    private boolean typing;  // This will indicate if someone is typing
    private String sender;   // The sender's username

    public ChatMessage() {}

    public ChatMessage(String content, boolean typing, String sender) {
        this.content = content;
        this.typing = typing;
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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
