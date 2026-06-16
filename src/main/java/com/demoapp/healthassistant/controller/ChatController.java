package com.demoapp.healthassistant.controller;

import com.demoapp.healthassistant.model.ChatRequest;
import com.demoapp.healthassistant.model.ChatResponse;
import com.demoapp.healthassistant.service.ChatOrchestrationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the chat API.
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatOrchestrationService orchestrationService;

    public ChatController(ChatOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    /**
     * Send a message and get a response.
     * POST /api/chat
     * Body: { "sessionId": "optional-uuid", "message": "user message" }
     */
    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = orchestrationService.processMessage(
                request.getSessionId(),
                request.getMessage()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Reset a session.
     * DELETE /api/chat/{sessionId}
     */
    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> resetSession(@PathVariable String sessionId) {
        orchestrationService.resetSession(sessionId);
        return ResponseEntity.noContent().build();
    }
}

