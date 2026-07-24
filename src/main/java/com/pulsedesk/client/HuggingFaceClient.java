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

    public String generate(String prompt) {
        String url = properties.getApiUrl() + "/" + properties.getModel();

        Map<String, Object> body = Map.of(
            "inputs", prompt,
            "parameters", Map.of(
                "max_new_tokens", 256,
                "temperature", 0.2
            )
        );

        List<?> response = restClient.post()
            .uri(url)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + properties.getApiToken())
            .body(body)
            .retrieve()
            .body(List.class);

            if(response == null || response.isEmpty()) {
                throw new IllegalStateException("Empty response from Hugging Face");
            }

            Object first = response.get(0);
            if(first instanceof Map<?, ?> map) {
                Object generated = map.get("generated_text");
                if(generated != null) {
                    return generated.toString();
                }
            }

            return String.valueOf(first);
    }
}