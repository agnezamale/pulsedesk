package com.pulsedesk.service;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.pulsedesk.model.Comment;
import com.pulsedesk.model.Ticket;
import com.pulsedesk.model.TriageResult;
import com.pulsedesk.repository.TicketRepository;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public Ticket createFromTriage(Comment comment, TriageResult triage) {
        Ticket ticket = new Ticket();
        ticket.setCommentId(comment.getId());
        ticket.setTitle(triage.getTitle());
        ticket.setCategory(triage.getCategory());
        ticket.setPriority(triage.getPriority());
        ticket.setSummary(triage.getSummary());
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAll() {
        return ticketRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ticket findById(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Ticket not found: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Ticket> findByCommentId(Long commentId) {
        return ticketRepository.findByCommentId(commentId);
    }
}