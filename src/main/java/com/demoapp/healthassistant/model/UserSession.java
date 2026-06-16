package com.demoapp.healthassistant.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds per-session conversation data.
 */
@Getter
public class UserSession {

    private final String sessionId;
    @Setter
    private ConversationState state;
    private final List<ChatMessage> symptomHistory;
    private final List<ChatMessage> doctorHistory;
    @Setter
    private String intakeSummary; // JSON summary from symptom collector

    public UserSession(String sessionId) {
        this.sessionId = sessionId;
        this.state = ConversationState.COLLECTING_SYMPTOMS;
        this.symptomHistory = new ArrayList<>();
        this.doctorHistory = new ArrayList<>();
    }

}

