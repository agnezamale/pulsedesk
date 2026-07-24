package com.pulsedesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pulsedesk.model.Category;
import com.pulsedesk.model.Priority;
import com.pulsedesk.model.TriageResult;

import tools.jackson.databind.json.JsonMapper;

class TriageResponseParserTest {

    private TriageResponseParser parser;

    @BeforeEach
    void setUp() {
        parser = new TriageResponseParser(new JsonMapper());
    }

    @Test
    void parsesTicketJson() {
        String raw = """
                {"shouldCreateTicket":true,"title":"Crash on save","category":"bug","priority":"high","summary":"App freezes on save"}
                """;

        TriageResult result = parser.parse(raw);

        assertTrue(result.shouldCreateTicket());
        assertEquals("Crash on save", result.getTitle());
        assertEquals(Category.BUG, result.getCategory());
        assertEquals(Priority.HIGH, result.getPriority());
        assertEquals("App freezes on save", result.getSummary());
    }

    @Test
    void parsesNoTicket() {
        String raw = "{\"shouldCreateTicket\":false,\"title\":\"\",\"category\":\"other\",\"priority\":\"low\",\"summary\":\"\"}";

        assertFalse(parser.parse(raw).shouldCreateTicket());
    }

    @Test
    void extractsJsonFromSurroundingText() {
        String raw = """
                Here is the result:
                {"shouldCreateTicket":true,"title":"Billing issue","category":"billing","priority":"medium","summary":"Double charge"}
                Thanks.
                """;

        TriageResult result = parser.parse(raw);

        assertTrue(result.shouldCreateTicket());
        assertEquals(Category.BILLING, result.getCategory());
        assertEquals(Priority.MEDIUM, result.getPriority());
    }

    @Test
    void returnsNoTicketForInvalidJson() {
        assertFalse(parser.parse("not-json-at-all").shouldCreateTicket());
    }
}
