package com.demoapp.healthassistant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.openai.api-key=test-key"
})
class HealthAssistantApplicationTests {

    @Test
    void contextLoads() {
    }
}

