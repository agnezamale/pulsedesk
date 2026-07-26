package com.pulsedesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsedesk.model.Comment;
import com.pulsedesk.model.TriageResult;
import com.pulsedesk.model.TriageStatus;
import com.pulsedesk.repository.CommentRepository;

@Service
public class CommentTriageJob {

    private static final Logger log = LoggerFactory.getLogger(CommentTriageJob.class);

    private final CommentRepository commentRepository;
    private final TriageService triageService;
    private final TicketService ticketService;

    public CommentTriageJob(
            CommentRepository commentRepository,
            TriageService triageService,
            TicketService ticketService
    ) {
        this.commentRepository = commentRepository;
        this.triageService = triageService;
        this.ticketService = ticketService;
    }

    @Async("triageExecutor")
    @Transactional
    public void triageAsync(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElse(null);
        if (comment == null) {
            log.warn("Skipping triage; comment not found: {}", commentId);
            return;
        }
        if (comment.getTriageStatus() == TriageStatus.COMPLETED) {
            return;
        }

        try {
            TriageResult triage = triageService.analyze(comment);
            if (triage.shouldCreateTicket()) {
                ticketService.createFromTriage(comment, triage);
                log.info("Async triage created ticket for comment {}", commentId);
            } else {
                log.info("Async triage decided no ticket for comment {}", commentId);
            }
        } catch (Exception e) {
            log.error("Async triage failed for comment {}", commentId, e);
        } finally {
            comment.setTriageStatus(TriageStatus.COMPLETED);
            commentRepository.save(comment);
        }
    }
}
