package com.demoapp.healthassistant.model;

/**
 * Simple chat message with role.
 */
public class ChatMessage {

    public enum Role {
        USER, ASSISTANT, SYSTEM
    }

    private final Role role;
    private final String content;

    public ChatMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }

    public Role getRole() {
        return role;
    }

    public String getContent() {
        return content;
    }
}

