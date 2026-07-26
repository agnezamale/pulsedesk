package com.pulsedesk.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.pulsedesk.model.Category;
import com.pulsedesk.model.Comment;
import com.pulsedesk.model.Priority;
import com.pulsedesk.model.TriageResult;

class DummyTriageServiceTest {

    private DummyTriageService triageService;

    @BeforeEach
    void setUp() {
        triageService = new DummyTriageService();
    }

    @Test
    void createsHighPriorityBillingTicket() {
        TriageResult result = triageService.analyze(comment("I was charged twice on my invoice"));

        assertTrue(result.shouldCreateTicket());
        assertEquals(Category.BILLING, result.getCategory());
        assertEquals(Priority.HIGH, result.getPriority());
    }

    @Test
    void createsMediumPriorityAccountTicket() {
        TriageResult result = triageService.analyze(comment("I cannot login to my account"));

        assertTrue(result.shouldCreateTicket());
        assertEquals(Category.ACCOUNT, result.getCategory());
        assertEquals(Priority.MEDIUM, result.getPriority());
    }

    @Test
    void createsHighPriorityBugTicket() {
        TriageResult result = triageService.analyze(comment("App crashes when opening settings"));

        assertTrue(result.shouldCreateTicket());
        assertEquals(Category.BUG, result.getCategory());
        assertEquals(Priority.HIGH, result.getPriority());
    }

    @Test
    void createsLowPriorityFeatureTicket() {
        TriageResult result = triageService.analyze(comment("It would be nice to add dark mode feature"));

        assertTrue(result.shouldCreateTicket());
        assertEquals(Category.FEATURE, result.getCategory());
        assertEquals(Priority.LOW, result.getPriority());
    }

    @Test
    void doesNotCreateTicketForCompliment() {
        TriageResult result = triageService.analyze(comment("I love this app, thanks!"));

        assertFalse(result.shouldCreateTicket());
    }

    @Test
    void doesNotCreateTicketForExpectedCloseBehavior() {
        TriageResult result = triageService.analyze(
                comment("The app closes when I press the Close button"));

        assertFalse(result.shouldCreateTicket());
    }

    @Test
    void createsBugTicketWhenCloseButtonDoesNothing() {
        TriageResult result = triageService.analyze(comment("Close button does nothing"));

        assertTrue(result.shouldCreateTicket());
        assertEquals(Category.BUG, result.getCategory());
        assertEquals(Priority.HIGH, result.getPriority());
    }

    @Test
    void doesNotCreateTicketForSuccessfulPaymentScreenClose() {
        TriageResult result = triageService.analyze(
                comment("Payment screen closes after successful payment"));

        assertFalse(result.shouldCreateTicket());
    }

    @Test
    void createsHighPriorityBillingTicketForFailedPaymentCharge() {
        TriageResult result = triageService.analyze(
                comment("Payment fails and I was charged anyway"));

        assertTrue(result.shouldCreateTicket());
        assertEquals(Category.BILLING, result.getCategory());
        assertEquals(Priority.HIGH, result.getPriority());
    }

    @Test
    void doesNotCreateTicketForNeutralText() {
        TriageResult result = triageService.analyze(comment("The UI looks clean and simple"));

        assertFalse(result.shouldCreateTicket());
    }

    private Comment comment(String text) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setChannel("web");
        return comment;
    }
}
