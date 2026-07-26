package com.pulsedesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.pulsedesk.client.HuggingFaceClient;
import com.pulsedesk.model.Comment;
import com.pulsedesk.model.TriageResult;

@Service
@Primary
@ConditionalOnProperty(name = "pulsedesk.triage.provider", havingValue = "huggingface", matchIfMissing = true)
public class HuggingFaceTriageService implements TriageService {

    private static final Logger log = LoggerFactory.getLogger(HuggingFaceTriageService.class);

    private final HuggingFaceClient huggingFaceClient;
    private final TriageResponseParser parser;
    private final DummyTriageService fallback;

    public HuggingFaceTriageService(
            HuggingFaceClient huggingFaceClient,
            TriageResponseParser parser,
            DummyTriageService fallback
    ) {
        this.huggingFaceClient = huggingFaceClient;
        this.parser = parser;
        this.fallback = fallback;
    }

    @Override
    public TriageResult analyze(Comment comment) {
        try {
            String prompt = buildPrompt(comment.getText());
            String raw = huggingFaceClient.generate(prompt);
            log.info("Hugging Face triage response received ({} chars)", raw.length());
            log.debug("Hugging Face raw response: {}", raw);
            TriageResult parsed = parser.parse(raw);
            if (!parsed.shouldCreateTicket() && raw.contains("shouldCreateTicket")) {
                log.info("Model decided no ticket should be created");
            }
            return parsed;
        } catch (Exception e) {
            log.warn("Hugging Face triage failed; falling back to keyword triage. Reason: {}", e.getMessage());
            return fallback.analyze(comment);
        }
    }

    private String buildPrompt(String commentText) {
        return """
                You are a support triage assistant for PulseDesk.
                Return ONLY one JSON object. No markdown. No explanations.

                Output schema:
                {
                  "shouldCreateTicket": false,
                  "title": "",
                  "category": "other",
                  "priority": "low",
                  "summary": ""
                }

                Allowed category: bug | feature | billing | account | other
                Allowed priority: low | medium | high

                STEP 1 — Decide shouldCreateTicket (true/false) BEFORE category.
                Ask: does this comment require a support agent to investigate or act?

                shouldCreateTicket = true ONLY if at least one is true:
                1) Unexpected failure (crash/freeze/error/broken/"not working" in a way that is not normal)
                2) Money problem (charge, invoice, refund, payment, subscription billing)
                3) Account access problem (cannot login, password reset failed, locked out)
                4) Clear request to add/change product functionality

                shouldCreateTicket = false if any is true:
                1) Expected/normal behavior (Close/X/Exit closes the app, labeled button does its job)
                2) Success flow that works as designed (payment screen closes after successful payment)
                3) Compliment / thanks / praise only
                4) Opinion or observation with no failure
                5) How-to / FAQ question with no broken behavior
                6) Vague text with no actionable problem

                If shouldCreateTicket=false, return empty title/summary and category="other", priority="low".

                STEP 2 — Only if shouldCreateTicket=true, choose category:
                - bug: unexpected technical failure
                - billing: money/payment/invoice/refund
                - account: login/password/access/ownership
                - feature: new capability request
                - other: actionable support need that fits none above

                STEP 3 — Only if shouldCreateTicket=true, choose priority:
                - high: unexpected crash/data loss/payment failure/cannot access account
                - medium: partial breakage or account friction
                - low: minor issue or feature idea

                Hard rules:
                - Prefer false when unsure.
                - Expected close/exit behavior is NEVER a ticket.
                - Successful payment UX (screen closes after success) is NEVER a ticket.
                - "closes" is not the same as "crashes".
                - Billing tickets require a money PROBLEM (failed charge, double charge, refund), not merely the word "payment".

                Few-shot examples:
                Comment: "I love this app, thanks!"
                {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}

                Comment: "The app closes when I press the Close button"
                {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}

                Comment: "How do I change my password?"
                {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}

                Comment: "The UI looks fine"
                {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}

                Comment: "App crashes when I open settings"
                {"shouldCreateTicket":true,"title":"Crash when opening settings","category":"bug","priority":"high","summary":"Unexpected crash when opening settings."}

                Comment: "The screen freezes every time I tap Save"
                {"shouldCreateTicket":true,"title":"Freeze when saving","category":"bug","priority":"high","summary":"Unexpected freeze when tapping Save."}

                Comment: "Payment screen closes after successful payment"
                {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}

                Comment: "Payment fails and I was charged anyway"
                {"shouldCreateTicket":true,"title":"Charged after failed payment","category":"billing","priority":"high","summary":"Payment failed but user was still charged."}

                Comment: "I was charged twice on my invoice"
                {"shouldCreateTicket":true,"title":"Double charge on invoice","category":"billing","priority":"high","summary":"User was charged twice."}

                Comment: "I cannot login to my account"
                {"shouldCreateTicket":true,"title":"Cannot log in","category":"account","priority":"medium","summary":"User cannot log in."}

                Comment: "Please add dark mode"
                {"shouldCreateTicket":true,"title":"Add dark mode","category":"feature","priority":"low","summary":"Request to add dark mode."}

                Now classify this comment:
                Comment: "%s"
                """.formatted(commentText);
    }
}
