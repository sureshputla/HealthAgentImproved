package com.demoapp.healthassistant.service;

import com.demoapp.healthassistant.model.ChatMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Agent 1: Symptom Collector
 * <p>
 * This agent interviews the user about their symptoms and demographics
 * (age, gender, location). When it has gathered enough information,
 * it produces a JSON summary and signals that intake is complete by
 * including the marker "##INTAKE_COMPLETE##" in its response.
 */
@Service
public class SymptomCollectorAgent {

    private static final String SYSTEM_PROMPT = """
            You are Disha, a compassionate and professional medical intake assistant. Your job is to interview
            the patient to collect the following information:

            1. **Symptoms**: What symptoms are they experiencing? Ask follow-up questions to understand
               severity, duration, and any related symptoms.
            2. **Age**: How old is the patient?
            3. **Gender**: What is the patient's gender?
            4. **Location**: Accept ANY one of these as a valid location input:
               - area/locality name
               - city/town
               - pincode/postal code
               The patient does NOT need to provide city/state/country if they already provided the area or pin code. Need to provide a hospital/clinic/doctor who is located within a 3 km radius of the given location.

            Guidelines:
            - In your first reply to the user every new conversation, briefly introduce yourself as Disha.
            - Be empathetic, warm, and professional.
            - Ask one question at a time — don't overwhelm the patient.
            - If the patient provides multiple pieces of information at once, acknowledge them.
            - Do NOT provide any medical diagnosis or treatment advice.
            - If pincode/postal code is present, treat location as complete. Do not ask for city/state/country again.
            - Once you have collected ALL four pieces of information (symptoms, age, gender, location),
              produce a summary in the following exact format at the END of your message:

              ##INTAKE_COMPLETE##
              {
                "symptoms": "<comma-separated list of symptoms>",
                "age": <age as number>,
                "gender": "<gender>",
                "location": "<area/city/pin code as provided by user>"
              }

            - Do NOT include ##INTAKE_COMPLETE## until you have ALL required information.
            - Before the marker, write a friendly message confirming you have all the details and
              that you will now help them find a suitable doctor/hospital nearby.
            """;
    public static final String INTAKE_COMPLETE = "##INTAKE_COMPLETE##";

    private final ChatModel chatModel;

    public SymptomCollectorAgent(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Send a user message and get the assistant's response, preserving conversation history.
     */
    public String chat(String userMessage, List<ChatMessage> history) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));

        // Add conversation history
        for (ChatMessage msg : history) {
            switch (msg.getRole()) {
                case USER -> messages.add(new UserMessage(msg.getContent()));
                case ASSISTANT -> messages.add(new AssistantMessage(msg.getContent()));
                case SYSTEM -> messages.add(new SystemMessage(msg.getContent()));
            }
        }

        // Add the current user message
        messages.add(new UserMessage(userMessage));

        Prompt prompt = new Prompt(messages);

        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    /**
     * Check if the agent's response indicates that intake is complete.
     */
    public boolean isIntakeComplete(String response) {
        return response != null && response.contains(INTAKE_COMPLETE);
    }

    /**
     * Extract the JSON summary from the agent's response.
     */
    public String extractIntakeSummary(String response) {
        int markerIndex = response.indexOf(INTAKE_COMPLETE);
        if (markerIndex == -1) {
            return null;
        }
        String afterMarker = response.substring(markerIndex + INTAKE_COMPLETE.length()).trim();
        // Extract JSON block
        int jsonStart = afterMarker.indexOf("{");
        int jsonEnd = afterMarker.lastIndexOf("}");
        if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart) {
            return afterMarker.substring(jsonStart, jsonEnd + 1);
        }
        return afterMarker;
    }

    /**
     * Get the user-friendly portion of the response (before the marker).
     */
    public String getUserFriendlyResponse(String response) {
        int markerIndex = response.indexOf(INTAKE_COMPLETE);
        if (markerIndex == -1) {
            return response;
        }
        return response.substring(0, markerIndex).trim();
    }
}
