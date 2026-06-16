# HealthAgentImproved

Multi-agent health assistant built with Spring Boot and Spring AI.

It uses two agents:
- **Symptom Collector Agent**: gathers symptoms and demographics (age, gender, location)
- **Doctor Finder Agent**: suggests specialist types and nearby care options

## Tech Stack

- Java 21
- Spring Boot 3.4.3
- Spring AI 1.0.0
- OpenAI API (`gpt-5-nano` by default)

## Prerequisites

- Java 21+
- Maven 3.8+
- OpenAI API key

## Configuration

The app reads your key from `OPENAI_API_KEY`.

`src/main/resources/application.yml` is configured as:

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-5-nano
          temperature: 0.7
```

To use a different model, update `spring.ai.openai.chat.options.model`.

## Intake Data Collected

The assistant collects:
- Symptoms
- Age
- Gender
- Location

For **location**, any one of the following is accepted:
- Area/locality
- City/town
- Pincode/postal code

If pincode is provided, the assistant treats location as complete and proceeds to doctor/hospital suggestions.

## Run Locally

Set your key in the current shell:

```bash
export OPENAI_API_KEY="your-openai-api-key"
```

Build and start:

```bash
mvn clean test
mvn spring-boot:run
```

App URL: `http://localhost:8081`

## API Endpoints

### Start / Continue Chat

`POST /api/chat`

```json
{
  "sessionId": null,
  "message": "I have had fever and cough for 2 days"
}
```

### Reset Session

`POST /api/chat/reset/{sessionId}`

## Project Structure

```text
HealthAgentImproved/
  src/main/java/com/demoapp/healthassistant/
    controller/
    exception/
    model/
    service/
  src/main/resources/
    application.yml
    static/index.html
  pom.xml
```

## Troubleshooting

- **401/403 from OpenAI**: check `OPENAI_API_KEY` and billing/project permissions.
- **429 from OpenAI**: your account/model rate limit is hit; retry later or use a lower-throughput pattern.
- **Model errors**: confirm the model name is available to your OpenAI account.

## License

Provided as-is for demo purposes.
