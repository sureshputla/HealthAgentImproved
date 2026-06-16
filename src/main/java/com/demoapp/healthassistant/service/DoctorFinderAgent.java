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
 * Agent 2: Doctor Finder
 * <p>
 * This agent receives the patient's intake summary (symptoms, age, gender, location)
 * and helps find suitable hospitals, clinics, or doctors near the patient's location.
 * It can also answer follow-up questions about the recommendations.
 */
@Service
public class DoctorFinderAgent {

    private static final String SYSTEM_PROMPT = """
            Your name is Disha, and you are a helpful healthcare facility finder assistant. You have received a patient's
            medical intake summary, which includes their symptoms, age, gender, and location reference.

            Location rules (important):
            - The location may be an area/locality, city, or pincode/postal code.
            - Treat any of these as valid input; do NOT ask for city/state/country again if pincode is already provided.
            - If pincode is provided, infer the likely city/area/state and provide recommendations around that location.

            Your job is to:
            1. Based on the patient's symptoms, age, gender, and location, recommend appropriate
               types of medical specialists they should see.
            2. Suggest specific hospitals, clinics, or doctors in or near their location.
            3. You are based out of India, so you can use the pin code to find the location.
            4. Provide practical information such as:
               - Type of specialist recommended
               - Names of well-known hospitals/clinics in their area
               - General advice on whether they should seek urgent/emergency care or can schedule
                 a regular appointment
               - Any preparatory steps before visiting the doctor

            Output:
                - Hospital or clinic name, if any
                - Doctor's name, if any
                - Address
                - Any process the user needs to follow before reaching the hospital or clinic
                - Symptoms he has provided

            Guidelines:
            - In your first doctor-finding response for a session, introduce yourself as Disha.
            - Be helpful, clear, and organised in your recommendations.
            - Format your response with clear sections and bullet points.
            - If you're not sure about specific facilities in the area, recommend the types of
              facilities and specialists to look for, and suggest how to find them (e.g., searching
              online, calling local health hotlines).
            - Do NOT provide medical diagnoses or prescribe treatments.
            - You can answer follow-up questions about your recommendations.
            - Always remind the patient that in case of an emergency, they should call emergency
              services immediately.
            """;

    private final ChatModel chatModel;

    public DoctorFinderAgent(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    /**
     * Start the doctor finding process with the intake summary.
     */
    public String findDoctors(String intakeSummary, List<ChatMessage> history) {
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

        // If history is empty, this is the initial call with the intake summary
        if (history.isEmpty()) {
            messages.add(new UserMessage(
                    "Here is the patient's intake summary. Please recommend suitable healthcare " +
                    "providers and facilities:\n\n" + intakeSummary
            ));
        }

        Prompt prompt = new Prompt(messages);
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    /**
     * Handle follow-up questions in the doctor-finding phase.
     */
    public String chat(String userMessage, String intakeSummary, List<ChatMessage> history) {
        List<Message> messages = new ArrayList<>();
        messages.add(new SystemMessage(SYSTEM_PROMPT));
        messages.add(new UserMessage(
                "Patient intake summary for context:\n" + intakeSummary
        ));

        // Add conversation history
        for (ChatMessage msg : history) {
            switch (msg.getRole()) {
                case USER -> messages.add(new UserMessage(msg.getContent()));
                case ASSISTANT -> messages.add(new AssistantMessage(msg.getContent()));
                case SYSTEM -> messages.add(new SystemMessage(msg.getContent()));
            }
        }

        // Add the current follow-up message
        messages.add(new UserMessage(userMessage));

        Prompt prompt = new Prompt(messages);
        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
