package com.pulsedesk.service;

import org.springframework.stereotype.Service;

import com.pulsedesk.model.Category;
import com.pulsedesk.model.Comment;
import com.pulsedesk.model.Priority;
import com.pulsedesk.model.TriageResult;

@Service
public class DummyTriageService implements TriageService {
    @Override
    public TriageResult analyze(Comment comment) {
        String text = comment.getText() == null ? "" : comment.getText().toLowerCase();

        // Compliment / no issue
        if (containsAny(text, "love", "great", "awesome", "thank", "thanks")) {
            return TriageResult.noTicket();
        }

        // Billing
        if (containsAny(text, "bill", "invoice", "charge", "payment", "refund")) {
            return TriageResult.ticket(
                    shorten("Billing issue: " + comment.getText(), 200),
                    Category.BILLING,
                    Priority.HIGH,
                    "User reported a billing-related problem."
            );
        }

        // Account
        if (containsAny(text, "login", "password", "account", "sign in", "cannot access")) {
            return TriageResult.ticket(
                    shorten("Account issue: " + comment.getText(), 200),
                    Category.ACCOUNT,
                    Priority.MEDIUM,
                    "User reported an account access problem."
            );
        }

        // Bug / crash / error
        if (containsAny(text, "crash", "bug", "error", "broken", "fail", "not working")) {
            return TriageResult.ticket(
                    shorten("Bug report: " + comment.getText(), 200),
                    Category.BUG,
                    Priority.HIGH,
                    "User reported a technical issue that likely needs a ticket."
            );
        }

        // Feature request
        if (containsAny(text, "please add", "would be nice", "feature", "wishlist")) {
            return TriageResult.ticket(
                    shorten("Feature request: " + comment.getText(), 200),
                    Category.FEATURE,
                    Priority.LOW,
                    "User suggested a new feature."
            );
        }

        return TriageResult.noTicket();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String shorten(String value, int max) {
        if (value.length() <= max) {
            return value;
        }
        return value.substring(0, max - 3) + "...";
    }
}