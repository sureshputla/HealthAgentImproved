package com.demoapp.healthassistant.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for the REST API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationError(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation error");
        return ResponseEntity.badRequest().body(Map.of("error", message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericError(Exception ex) {
        if (isQuotaExceeded(ex)) {
            log.warn("Gemini API quota exceeded: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of(
                            "error",
                            "The AI service is temporarily unavailable due to quota limits. " +
                            "Please wait a moment and try again, or check your Gemini API quota at https://ai.dev/rate-limit."
                    ));
        }

        if (isUnauthorized(ex)) {
            log.warn("Gemini API authentication error: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of("error", "Invalid or missing GOOGLE_API_KEY. Please check your API key configuration."));
        }

        log.error("Unexpected error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "An unexpected error occurred. Please try again."));
    }

    private boolean isQuotaExceeded(Throwable throwable) {
        return containsInChain(throwable, msg ->
                msg.contains("429") || msg.contains("RESOURCE_EXHAUSTED") || msg.contains("quota"));
    }

    private boolean isUnauthorized(Throwable throwable) {
        return containsInChain(throwable, msg ->
                msg.contains("401") || msg.contains("403") || msg.contains("API_KEY_INVALID"));
    }

    private boolean containsInChain(Throwable throwable, java.util.function.Predicate<String> predicate) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && predicate.test(message)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

