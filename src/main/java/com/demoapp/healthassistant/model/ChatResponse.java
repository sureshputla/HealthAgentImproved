package com.demoapp.healthassistant.model;

/**
 * Chat response returned to the client.
 */
public class ChatResponse {

    private String sessionId;
    private String reply;
    private String state;

    public ChatResponse() {
    }

    public ChatResponse(String sessionId, String reply, String state) {
        this.sessionId = sessionId;
        this.reply = reply;
        this.state = state;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

