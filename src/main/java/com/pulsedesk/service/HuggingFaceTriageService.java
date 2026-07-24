package com.pulsedesk.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import com.pulsedesk.client.HuggingFaceClient;
import com.pulsedesk.model.Comment;
import com.pulsedesk.model.TriageResult;

@Service
@Primary
public class HuggingFaceTriageService implements TriageService {

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
			System.out.println("HF raw response: " + raw);
			return parser.parse(raw);
		} catch (Exception e) {
			System.out.println("HF failed, using dummy. Reason: " + e.getMessage());
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
