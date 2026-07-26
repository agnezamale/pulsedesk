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

        // Expected UI / success flow should not create tickets
        if (isExpectedCloseBehavior(text) || isExpectedSuccessFlow(text)) {
            return TriageResult.noTicket();
        }

        // Billing — only when there is a real money problem, not just the word "payment"
        if (isBillingProblem(text)) {
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
        if (containsAny(text, "crash", "bug", "error", "broken", "fail", "not working", "freeze")) {
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

    private boolean isExpectedCloseBehavior(String text) {
        boolean mentionsCloseAction = containsAny(
                text,
                "close button",
                "press close",
                "pressing close",
                "tap close",
                "click close",
                "hit close",
                "press x",
                "press exit",
                "tap exit",
                "click exit"
        );
        boolean mentionsClosing = containsAny(text, "closes", "closed", "closing", "exits", "exit the app", "quits");
        boolean mentionsUnexpectedFailure = containsAny(text, "crash", "freeze", "error", "broken", "not working", "fail");
        return mentionsCloseAction && mentionsClosing && !mentionsUnexpectedFailure;
    }

    private boolean isExpectedSuccessFlow(String text) {
        boolean success = containsAny(text, "successful", "successfully", "success", "after payment", "completed");
        boolean closesOrRedirects = containsAny(text, "closes", "closed", "closing", "redirect", "returns to");
        boolean problem = containsAny(
                text,
                "fail",
                "failed",
                "error",
                "crash",
                "freeze",
                "charged anyway",
                "charged twice",
                "double charge",
                "refund",
                "wrong amount",
                "not working"
        );
        return success && closesOrRedirects && !problem;
    }

    private boolean isBillingProblem(String text) {
        boolean billingWord = containsAny(text, "bill", "invoice", "charge", "payment", "refund", "subscription");
        if (!billingWord) {
            return false;
        }
        // Mentions payment/billing in a success/normal flow without a problem
        if (isExpectedSuccessFlow(text)) {
            return false;
        }
        return containsAny(
                text,
                "fail",
                "failed",
                "twice",
                "double",
                "refund",
                "charged anyway",
                "overcharged",
                "wrong amount",
                "unexpected charge",
                "billed after",
                "cancelled",
                "cancel"
        ) || containsAny(text, "invoice", "refund");
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
