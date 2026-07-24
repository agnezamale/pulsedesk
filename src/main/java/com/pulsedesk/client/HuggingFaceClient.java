package com.pulsedesk.client;

import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.pulsedesk.config.HuggingFaceProperties;

@Component
public class HuggingFaceClient {

	private final RestClient restClient;
	private final HuggingFaceProperties properties;

	public HuggingFaceClient(RestClient.Builder builder, HuggingFaceProperties properties) {
		this.properties = properties;
		this.restClient = builder.build();
	}

	@SuppressWarnings("unchecked")
	public String generate(String prompt) {
		if (properties.getApiToken() == null || properties.getApiToken().isBlank()) {
			throw new IllegalStateException("HF_API_TOKEN is not set");
		}

		Map<String, Object> body = Map.of(
				"model", properties.getModel(),
				"messages", List.of(
						Map.of("role", "user", "content", prompt)
				),
				"max_tokens", 256,
				"temperature", 0.2
		);

		Map<String, Object> response = restClient.post()
				.uri(properties.getApiUrl())
				.contentType(MediaType.APPLICATION_JSON)
				.header("Authorization", "Bearer " + properties.getApiToken())
				.body(body)
				.retrieve()
				.body(Map.class);

		if (response == null) {
			throw new IllegalStateException("Empty response from Hugging Face");
		}

		Object choicesObj = response.get("choices");
		if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) {
			throw new IllegalStateException("Hugging Face response missing choices: " + response);
		}

		Object first = choices.get(0);
		if (first instanceof Map<?, ?> choice) {
			Object messageObj = choice.get("message");
			if (messageObj instanceof Map<?, ?> message) {
				Object content = message.get("content");
				if (content != null && !content.toString().isBlank()) {
					return content.toString();
				}
			}
		}

		throw new IllegalStateException("Hugging Face response missing message content: " + response);
	}
}
