package com.demoapp.healthassistant.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds per-session conversation data.
 */
public class UserSession {

    private final String sessionId;
    private ConversationState state;
    private final List<ChatMessage> symptomHistory;
    private final List<ChatMessage> doctorHistory;
    private String intakeSummary; // JSON summary from symptom collector

    public UserSession(String sessionId) {
        this.sessionId = sessionId;
        this.state = ConversationState.COLLECTING_SYMPTOMS;
        this.symptomHistory = new ArrayList<>();
        this.doctorHistory = new ArrayList<>();
    }

    public String getSessionId() {
        return sessionId;
    }

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }

    public List<ChatMessage> getSymptomHistory() {
        return symptomHistory;
    }

    public List<ChatMessage> getDoctorHistory() {
        return doctorHistory;
    }

    public String getIntakeSummary() {
        return intakeSummary;
    }

    public void setIntakeSummary(String intakeSummary) {
        this.intakeSummary = intakeSummary;
    }
}

