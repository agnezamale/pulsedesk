package com.pulsedesk.service;

import org.springframework.stereotype.Component;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.pulsedesk.model.Category;
import com.pulsedesk.model.Priority;
import com.pulsedesk.model.TriageResult;

@Component
public class TriageResponseParser {

    private final ObjectMapper objectMapper;

    public TriageResponseParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public TriageResult parse(String raw) {
        try {
            String json = extractJson(raw);
            JsonNode node = objectMapper.readTree(json);

            boolean shouldCreate = node.path("shouldCreateTicket").asBoolean(false);
            if (!shouldCreate) {
                return TriageResult.noTicket();
            }

            String title = node.path("title").asText("Support ticket");
            String summary = node.path("summary").asText("User reported an issue.");
            Category category = parseCategory(node.path("category").asText("other"));
            Priority priority = parsePriority(node.path("priority").asText("medium"));

            return TriageResult.ticket(shorten(title, 200), category, priority, shorten(summary, 1000));
        } catch (Exception e) {
            // Safe fallback if model returns messy text
            return TriageResult.noTicket();
        }
    }

    private String extractJson(String raw) {
        int start = raw.indexOf('{');
        int end = raw.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return raw.substring(start, end + 1);
        }
        return raw;
    }

    private Category parseCategory(String value) {
        return switch (value.trim().toLowerCase()) {
            case "bug" -> Category.BUG;
            case "feature" -> Category.FEATURE;
            case "billing" -> Category.BILLING;
            case "account" -> Category.ACCOUNT;
            default -> Category.OTHER;
        };
    }

    private Priority parsePriority(String value) {
        return switch (value.trim().toLowerCase()) {
            case "low" -> Priority.LOW;
            case "high" -> Priority.HIGH;
            default -> Priority.MEDIUM;
        };
    }

    private String shorten(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }
}