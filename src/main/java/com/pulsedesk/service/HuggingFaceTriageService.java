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
                You classify support comments. Return ONLY one JSON object. No markdown. No extra text.

                JSON schema:
                {
                  "shouldCreateTicket": true,
                  "title": "short title",
                  "category": "bug",
                  "priority": "high",
                  "summary": "one short sentence"
                }

                Allowed category values: bug, feature, billing, account, other
                Allowed priority values: low, medium, high

                Category rules (choose exactly one):
                - bug: crashes, freezes, errors, broken UI, "not working", save/login failures that are technical
                - billing: invoices, charges, refunds, payments, subscriptions, pricing
                - account: password reset, cannot sign in, access permissions, profile ownership
                - feature: requests for new functionality or improvements
                - other: anything else that still needs support attention

                Important:
                - If the comment mentions crash/freeze/error/bug, category MUST be "bug" unless it is clearly about money/payment/invoice.
                - "save", "settings", "button", "screen" alone do NOT mean billing.
                - Compliments, praise, or thanks only => shouldCreateTicket=false
                - Real problems or feature requests => shouldCreateTicket=true
                - priority high: crashes, data loss, payment failures, cannot access account
                - priority medium: partial breakage, account issues
                - priority low: minor issues or feature ideas

                Examples:
                Comment: "I love this app, thanks!"
                {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}

                Comment: "App crashes when I open settings"
                {"shouldCreateTicket":true,"title":"Crash when opening settings","category":"bug","priority":"high","summary":"The app crashes when opening settings."}

                Comment: "The screen freezes every time I tap Save"
                {"shouldCreateTicket":true,"title":"Freeze when saving","category":"bug","priority":"high","summary":"The screen freezes when tapping Save."}

                Comment: "I was charged twice on my invoice"
                {"shouldCreateTicket":true,"title":"Double charge on invoice","category":"billing","priority":"high","summary":"User reports being charged twice."}

                Comment: "I cannot login to my account"
                {"shouldCreateTicket":true,"title":"Cannot log in","category":"account","priority":"medium","summary":"User cannot log into their account."}

                Comment: "Please add dark mode"
                {"shouldCreateTicket":true,"title":"Add dark mode","category":"feature","priority":"low","summary":"User requests a dark mode feature."}

                Comment: "%s"
                """.formatted(commentText);
    }
}
