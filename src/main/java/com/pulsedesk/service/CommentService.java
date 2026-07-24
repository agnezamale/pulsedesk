package com.pulsedesk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pulsedesk.model.Comment;
import com.pulsedesk.model.CommentResponse;
import com.pulsedesk.model.CreateCommentRequest;
import com.pulsedesk.model.Ticket;
import com.pulsedesk.model.TriageResult;
import com.pulsedesk.repository.CommentRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final DummyTriageService triageService;
    private final TicketService ticketService;

    public CommentService(
            CommentRepository commentRepository,
            DummyTriageService triageService,
            TicketService ticketService
    ) {
        this.commentRepository = commentRepository;
        this.triageService = triageService;
        this.ticketService = ticketService;
    }

    @Transactional
    public CommentResponse create(CreateCommentRequest request) {
        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setChannel(request.getChannel());
        comment = commentRepository.save(comment);

        TriageResult triage = triageService.analyze(comment);
        Ticket ticket = null;
        if (triage.shouldCreateTicket()) {
            ticket = ticketService.createFromTriage(comment, triage);
        }

        return CommentResponse.from(comment, ticket);
    }

    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }
}