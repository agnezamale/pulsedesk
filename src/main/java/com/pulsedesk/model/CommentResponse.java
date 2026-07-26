package com.pulsedesk.model;

import java.time.Instant;

public class CommentResponse {

    private Long id;
    private String text;
    private String channel;
    private Instant createdAt;
    private boolean triagePending;
    private boolean ticketCreated;
    private Long ticketId;

    public static CommentResponse pending(Comment comment) {
        CommentResponse response = new CommentResponse();
        response.id = comment.getId();
        response.text = comment.getText();
        response.channel = comment.getChannel();
        response.createdAt = comment.getCreatedAt();
        response.triagePending = true;
        response.ticketCreated = false;
        response.ticketId = null;
        return response;
    }

    public static CommentResponse from(Comment comment, Ticket ticket) {
        CommentResponse response = new CommentResponse();
        response.id = comment.getId();
        response.text = comment.getText();
        response.channel = comment.getChannel();
        response.createdAt = comment.getCreatedAt();
        response.triagePending = comment.getTriageStatus() == TriageStatus.PENDING;
        response.ticketCreated = ticket != null;
        response.ticketId = ticket != null ? ticket.getId() : null;
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getChannel() {
        return channel;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isTriagePending() {
        return triagePending;
    }

    public boolean isTicketCreated() {
        return ticketCreated;
    }

    public Long getTicketId() {
        return ticketId;
    }
}
