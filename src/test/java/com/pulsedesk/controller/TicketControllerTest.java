package com.pulsedesk.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.pulsedesk.model.Category;
import com.pulsedesk.model.Priority;
import com.pulsedesk.model.Ticket;
import com.pulsedesk.service.TicketService;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ticketController).build();
    }

    @Test
    void listTicketsReturnsOk() throws Exception {
        when(ticketService.findAll()).thenReturn(List.of(sampleTicket(1L)));

        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Crash on save"))
                .andExpect(jsonPath("$[0].priority").value("HIGH"));
    }

    @Test
    void getTicketByIdReturnsOk() throws Exception {
        when(ticketService.findById(1L)).thenReturn(sampleTicket(1L));

        mockMvc.perform(get("/tickets/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.category").value("BUG"));
    }

    @Test
    void getMissingTicketReturnsNotFound() throws Exception {
        when(ticketService.findById(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found: 99"));

        mockMvc.perform(get("/tickets/99"))
                .andExpect(status().isNotFound());
    }

    private Ticket sampleTicket(Long id) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setCommentId(10L);
        ticket.setTitle("Crash on save");
        ticket.setCategory(Category.BUG);
        ticket.setPriority(Priority.HIGH);
        ticket.setSummary("App freezes on save");
        ticket.setCreatedAt(Instant.parse("2026-07-24T12:00:00Z"));
        return ticket;
    }
}
