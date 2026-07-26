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
                Triage this PulseDesk product comment. Reply with ONLY one JSON object (no markdown):
                {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}
                category: bug|feature|billing|account|other
                priority: low|medium|high

                Create a ticket ONLY for actionable product issues:
                - bug: unexpected failure, freeze, crash, or control that fails (e.g. Close does nothing)
                - billing: real money problem (failed/double charge, refund) — not just the word "payment"
                - account: cannot log in / locked out
                - feature: request about THIS product only (not unrelated wishes)

                Do NOT create a ticket for:
                - expected UX (Close closes the app; payment screen closes after successful payment)
                - compliments, opinions, how-to questions, vague text
                - "closes" ≠ "crashes"

                If no ticket: empty title/summary, category other, priority low.
                Prefer no ticket when unsure, EXCEPT a labeled control that does nothing / doesn't work.

                Examples:
                "I love this app, thanks!" -> {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}
                "The app closes when I press Close" -> {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}
                "Close button does nothing" -> {"shouldCreateTicket":true,"title":"Close button does nothing","category":"bug","priority":"high","summary":"Close control is unresponsive."}
                "Payment screen closes after successful payment" -> {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}
                "Payment fails and I was charged anyway" -> {"shouldCreateTicket":true,"title":"Charged after failed payment","category":"billing","priority":"high","summary":"Charged despite failed payment."}
                "App crashes when I open settings" -> {"shouldCreateTicket":true,"title":"Crash opening settings","category":"bug","priority":"high","summary":"Unexpected crash in settings."}
                "I cannot login" -> {"shouldCreateTicket":true,"title":"Cannot log in","category":"account","priority":"medium","summary":"User cannot log in."}
                "Please add dark mode" -> {"shouldCreateTicket":true,"title":"Add dark mode","category":"feature","priority":"low","summary":"Request dark mode."}
                "It would be nice to play Fortnite" -> {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}

                Comment: "%s"
                """.formatted(commentText);
    }
}
