package com.demoapp.healthassistant.service;

import com.demoapp.healthassistant.model.ChatMessage;
import com.demoapp.healthassistant.model.ChatResponse;
import com.demoapp.healthassistant.model.ConversationState;
import com.demoapp.healthassistant.model.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Orchestrates the conversation flow between the two LLM agents.
 * <p>
 * Flow:
 * 1. User messages are first routed to SymptomCollectorAgent
 * 2. Once symptom collection is complete, the summary is forwarded to DoctorFinderAgent
 * 3. Any follow-up messages go to DoctorFinderAgent
 */
@Service
public class ChatOrchestrationService {

    private static final Logger log = LoggerFactory.getLogger(ChatOrchestrationService.class);

    private final SymptomCollectorAgent symptomCollector;
    private final DoctorFinderAgent doctorFinder;
    private final ConcurrentHashMap<String, UserSession> sessions = new ConcurrentHashMap<>();

    public ChatOrchestrationService(SymptomCollectorAgent symptomCollector,
                                     DoctorFinderAgent doctorFinder) {
        this.symptomCollector = symptomCollector;
        this.doctorFinder = doctorFinder;
    }

    /**
     * Process a user chat message within the given session.
     */
    public ChatResponse processMessage(String sessionId, String userMessage) {
        // Create or retrieve session
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        final String finalSessionId = sessionId;
        UserSession session = sessions.computeIfAbsent(finalSessionId, UserSession::new);

        return switch (session.getState()) {
            case COLLECTING_SYMPTOMS -> handleSymptomCollection(session, userMessage);
            case FINDING_DOCTORS -> handleDoctorFinding(session, userMessage);
            case COMPLETE -> handleComplete(session, userMessage);
        };
    }

    private ChatResponse handleSymptomCollection(UserSession session, String userMessage) {
        log.info("Session {}: Collecting symptoms", session.getSessionId());

        // Get response from symptom collector
        String agentResponse = symptomCollector.chat(userMessage, session.getSymptomHistory());

        // Save conversation history
        session.getSymptomHistory().add(new ChatMessage(ChatMessage.Role.USER, userMessage));

        if (symptomCollector.isIntakeComplete(agentResponse)) {
            // Extract summary and transition to doctor finding
            String summary = symptomCollector.extractIntakeSummary(agentResponse);
            String friendlyResponse = symptomCollector.getUserFriendlyResponse(agentResponse);
            session.setIntakeSummary(summary);
            session.getSymptomHistory().add(new ChatMessage(ChatMessage.Role.ASSISTANT, friendlyResponse));

            log.info("Session {}: Intake complete. Summary: {}", session.getSessionId(), summary);

            // Transition state
            session.setState(ConversationState.FINDING_DOCTORS);

            // Automatically invoke the doctor finder
            String doctorResponse = doctorFinder.findDoctors(summary, session.getDoctorHistory());
            session.getDoctorHistory().add(new ChatMessage(ChatMessage.Role.USER,
                    "Patient intake summary:\n" + summary));
            session.getDoctorHistory().add(new ChatMessage(ChatMessage.Role.ASSISTANT, doctorResponse));

            // Combine responses
            String combinedResponse = friendlyResponse + "\n\n---\n\n" + doctorResponse;

            return new ChatResponse(session.getSessionId(), combinedResponse,
                    session.getState().name());
        } else {
            session.getSymptomHistory().add(new ChatMessage(ChatMessage.Role.ASSISTANT, agentResponse));
            return new ChatResponse(session.getSessionId(), agentResponse,
                    session.getState().name());
        }
    }

    private ChatResponse handleDoctorFinding(UserSession session, String userMessage) {
        log.info("Session {}: Doctor finding follow-up", session.getSessionId());

        String response = doctorFinder.chat(userMessage, session.getIntakeSummary(),
                session.getDoctorHistory());

        session.getDoctorHistory().add(new ChatMessage(ChatMessage.Role.USER, userMessage));
        session.getDoctorHistory().add(new ChatMessage(ChatMessage.Role.ASSISTANT, response));

        return new ChatResponse(session.getSessionId(), response, session.getState().name());
    }

    private ChatResponse handleComplete(UserSession session, String userMessage) {
        // Allow continued conversation in doctor finding mode
        session.setState(ConversationState.FINDING_DOCTORS);
        return handleDoctorFinding(session, userMessage);
    }

    /**
     * Get the current state of a session.
     */
    public ConversationState getSessionState(String sessionId) {
        UserSession session = sessions.get(sessionId);
        return session != null ? session.getState() : null;
    }

    /**
     * Reset/delete a session.
     */
    public void resetSession(String sessionId) {
        sessions.remove(sessionId);
    }
}

