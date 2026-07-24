package com.pulsedesk.model;

public class TriageResult {

    private final boolean shouldCreateTicket;
    private final String title;
    private final Category category;
    private final Priority priority;
    private final String summary;

    private TriageResult(
        boolean shouldCreateTicket,
        String title,
        Category category,
        Priority priority,
        String summary
    ) {
        this.shouldCreateTicket = shouldCreateTicket;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.summary = summary;
    }

    public static TriageResult noTicket() {
        return new TriageResult(false, null, null, null, null);
    }

    public static TriageResult ticket(
        String title,
        Category category,
        Priority priority,
        String summary
    ) {
        return new TriageResult(true, title, category, priority, summary);
    }

    public boolean shouldCreateTicket() {
        return shouldCreateTicket;
    }

    public String getTitle() {
        return title;
    }

    public Category getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public String getSummary() {
        return summary;
    }
}