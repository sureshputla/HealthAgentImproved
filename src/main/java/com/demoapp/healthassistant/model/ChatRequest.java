package com.demoapp.healthassistant.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * Incoming chat request from the client.
 */
@Setter
@Getter
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

}

