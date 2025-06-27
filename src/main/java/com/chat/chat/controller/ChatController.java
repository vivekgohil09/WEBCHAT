package com.chat.chat.controller;

import com.chat.chat.model.ChatMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // For converting user list to JSON string

    // Map to track active users per room. Key: roomName, Value: Set of usernames
    private final Map<String, Set<String>> roomActiveUsers = new ConcurrentHashMap<>();

    // A map to keep track of which user is in which room, for disconnect handling
    // Key: WebSocket Session ID, Value: Map containing "username" and "roomName"
    private final Map<String, Map<String, String>> sessionRoomMap = new ConcurrentHashMap<>();


    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles initial connection messages from clients when they enter a room.
     * This method receives the 'JOIN' message from the frontend and broadcasts it
     * to all participants in the specified room. It also updates and broadcasts
     * the list of active users.
     *
     * @param chatMessage The ChatMessage object containing sender and type (JOIN).
     * @param room The name of the chat room.
     * @param headerAccessor Used to access session attributes (e.g., session ID, username).
     */
    @MessageMapping("/connect/{room}")
    public void joinRoom(@Payload ChatMessage chatMessage,
                         @DestinationVariable String room,
                         SimpMessageHeaderAccessor headerAccessor) {
        // Ensure the message type is JOIN from the client
        chatMessage.setType(ChatMessage.MessageType.JOIN);

        // Store user and room info in session attributes for disconnect handling
        String sessionId = headerAccessor.getSessionId();
        String username = chatMessage.getSender();

        if (sessionId != null) {
            Map<String, String> userInfo = new ConcurrentHashMap<>();
            userInfo.put("username", username);
            userInfo.put("roomName", room);
            sessionRoomMap.put(sessionId, userInfo);

            // Set session attributes so they can be retrieved on disconnect
            if (headerAccessor.getSessionAttributes() != null) {
                headerAccessor.getSessionAttributes().put("username", username);
                headerAccessor.getSessionAttributes().put("roomName", room);
            }
        }

        // Add the joining user to the active users set for this room
        roomActiveUsers.computeIfAbsent(room, k -> ConcurrentHashMap.newKeySet()).add(username);

        // Broadcast the JOIN message to the room's topic
        messagingTemplate.convertAndSend("/topic/room/" + room, chatMessage);

        // After a user joins, send an updated list of all active users to the room
        sendUserListUpdate(room);
    }


    /**
     * Handles sending of regular chat messages to a specific room.
     *
     * @param message The ChatMessage object containing sender, content.
     * @param room The name of the chat room.
     */
    @MessageMapping("/sendMessage/{room}")
    public void sendMessage(@Payload ChatMessage message, @DestinationVariable String room) {
        // Ensure the message type is CHAT for regular messages
        message.setType(ChatMessage.MessageType.CHAT);
        messagingTemplate.convertAndSend("/topic/room/" + room, message);
    }

    /**
     * Handles typing indicator notifications.
     *
     * @param message The ChatMessage object containing sender and typing status.
     * @param room The name of the chat room.
     */
    @MessageMapping("/typing/{room}")
    public void sendTypingIndicator(@Payload ChatMessage message, @DestinationVariable String room) {
        // No specific type needed for typing messages, as the frontend handles based on 'typing' boolean
        messagingTemplate.convertAndSend("/topic/typing/" + room, message);
    }

    /**
     * Handles clearing chat messages in a room.
     * This method can be triggered by an admin or specific user action to clear the chat history for all.
     *
     * @param room The name of the chat room.
     */
    @MessageMapping("/clearChat/{room}")
    public void clearChat(@DestinationVariable String room) {
        // Send a specific message to all users in the room to clear their chat window
        // The frontend will need to listen to "/topic/clear/{room}" and clear its display.
        messagingTemplate.convertAndSend("/topic/clear/" + room, "clear");
    }

    /**
     * Listens for WebSocket session disconnect events.
     * When a user disconnects, it broadcasts a 'LEAVE' message to the room they were in
     * and also sends an updated list of active users.
     *
     * @param event The SessionDisconnectEvent.
     */
    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            Map<String, String> userInfo = sessionRoomMap.remove(sessionId); // Remove from map
            if (userInfo != null) {
                String username = userInfo.get("username");
                String roomName = userInfo.get("roomName");

                if (username != null && roomName != null) {
                    System.out.println("User " + username + " left room " + roomName);

                    // Remove the leaving user from the active users set for this room
                    Set<String> usersInRoom = roomActiveUsers.get(roomName);
                    if (usersInRoom != null) {
                        usersInRoom.remove(username);
                        // If the room becomes empty, optionally remove the room entry from roomActiveUsers
                        if (usersInRoom.isEmpty()) {
                            roomActiveUsers.remove(roomName);
                        }
                    }

                    ChatMessage leaveMessage = new ChatMessage();
                    leaveMessage.setType(ChatMessage.MessageType.LEAVE);
                    leaveMessage.setSender(username);
                    // Broadcast the LEAVE message to the room's topic
                    messagingTemplate.convertAndSend("/topic/room/" + roomName, leaveMessage);

                    // After a user leaves, send an updated list of all active users to the room
                    sendUserListUpdate(roomName);
                }
            }
        }
    }

    /**
     * Helper method to get the current list of active users for a given room.
     *
     * @param roomName The name of the chat room.
     * @return A Set of usernames active in the room.
     */
    private Set<String> getActiveUsersInRoom(String roomName) {
        return roomActiveUsers.getOrDefault(roomName, Collections.emptySet());
    }

    /**
     * Sends a USER_LIST_UPDATE message to all clients in a specific room.
     * The message content will be a JSON string array of current usernames.
     *
     * @param roomName The name of the chat room to send the update to.
     */
    private void sendUserListUpdate(String roomName) {
        Set<String> users = getActiveUsersInRoom(roomName);
        try {
            String usersJson = objectMapper.writeValueAsString(users);
            ChatMessage userListUpdateMessage = new ChatMessage();
            userListUpdateMessage.setType(ChatMessage.MessageType.USER_LIST_UPDATE);
            userListUpdateMessage.setSender("server"); // Or a more appropriate server sender name
            userListUpdateMessage.setContent(usersJson);
            messagingTemplate.convertAndSend("/topic/room/" + roomName, userListUpdateMessage);
        } catch (JsonProcessingException e) {
            System.err.println("Error converting user list to JSON: " + e.getMessage());
        }
    }
}
