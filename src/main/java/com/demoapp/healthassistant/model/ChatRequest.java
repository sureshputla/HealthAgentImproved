package com.demoapp.healthassistant.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Incoming chat request from the client.
 */
public class ChatRequest {

    private String sessionId;

    @NotBlank(message = "Message must not be blank")
    private String message;

    public ChatRequest() {
    }

    public ChatRequest(String sessionId, String message) {
        this.sessionId = sessionId;
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

