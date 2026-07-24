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
                Analyze the user comment and return ONLY valid JSON (no markdown).
                Schema:
                {
                  "shouldCreateTicket": true|false,
                  "title": "short title",
                  "category": "bug|feature|billing|account|other",
                  "priority": "low|medium|high",
                  "summary": "one short sentence"
                }
                Rules:
                - Compliments or thanks => shouldCreateTicket=false
                - Real problems/requests => shouldCreateTicket=true
                - category must be one of the allowed values
                - priority must be one of the allowed values
                Examples:
                Comment: "I love this app, thanks!"
                {"shouldCreateTicket":false,"title":"","category":"other","priority":"low","summary":""}
                Comment: "App crashes when I pay with Visa"
                {"shouldCreateTicket":true,"title":"Payment crash with Visa","category":"billing","priority":"high","summary":"App crashes during Visa payment."}
                Comment: "%s"
                """.formatted(commentText);
    }
}
