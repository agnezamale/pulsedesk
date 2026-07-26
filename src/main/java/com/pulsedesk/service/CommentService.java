package com.pulsedesk.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import com.pulsedesk.model.Comment;
import com.pulsedesk.model.CommentResponse;
import com.pulsedesk.model.CreateCommentRequest;
import com.pulsedesk.model.Ticket;
import com.pulsedesk.model.TriageStatus;
import com.pulsedesk.model.TriageStatusResponse;
import com.pulsedesk.repository.CommentRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentTriageJob commentTriageJob;
    private final TicketService ticketService;

    public CommentService(
            CommentRepository commentRepository,
            CommentTriageJob commentTriageJob,
            TicketService ticketService
    ) {
        this.commentRepository = commentRepository;
        this.commentTriageJob = commentTriageJob;
        this.ticketService = ticketService;
    }

    @Transactional
    public CommentResponse create(CreateCommentRequest request) {
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setChannel(request.getChannel());
        comment.setTriageStatus(TriageStatus.PENDING);
        comment = commentRepository.save(comment);

        Long commentId = comment.getId();
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    commentTriageJob.triageAsync(commentId);
                }
            });
        } else {
            commentTriageJob.triageAsync(commentId);
        }

        return CommentResponse.pending(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public TriageStatusResponse getTriageStatus(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Comment not found: " + commentId));

        Ticket ticket = ticketService.findByCommentId(commentId).orElse(null);
        return new TriageStatusResponse(
                comment.getId(),
                comment.getTriageStatus(),
                ticket != null,
                ticket != null ? ticket.getId() : null
        );
    }
}
