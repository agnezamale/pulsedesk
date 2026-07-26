package com.pulsedesk.model;

public class TriageStatusResponse {

    private Long commentId;
    private TriageStatus triageStatus;
    private boolean ticketCreated;
    private Long ticketId;

    public TriageStatusResponse(
            Long commentId,
            TriageStatus triageStatus,
            boolean ticketCreated,
            Long ticketId
    ) {
        this.commentId = commentId;
        this.triageStatus = triageStatus;
        this.ticketCreated = ticketCreated;
        this.ticketId = ticketId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public TriageStatus getTriageStatus() {
        return triageStatus;
    }

    public boolean isTicketCreated() {
        return ticketCreated;
    }

    public Long getTicketId() {
        return ticketId;
    }
}
