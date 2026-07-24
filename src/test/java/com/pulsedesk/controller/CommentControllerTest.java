package com.pulsedesk.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.pulsedesk.model.Comment;
import com.pulsedesk.model.CommentResponse;
import com.pulsedesk.service.CommentService;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CommentService commentService;

    @InjectMocks
    private CommentController commentController;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(commentController)
                .setValidator(validator)
                .build();
    }

    @Test
    void createCommentReturnsCreated() throws Exception {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("App crashes on login");
        comment.setChannel("web");
        comment.setCreatedAt(Instant.parse("2026-07-24T12:00:00Z"));

        when(commentService.create(any())).thenReturn(CommentResponse.from(comment, null));

        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"App crashes on login","channel":"web"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("App crashes on login"))
                .andExpect(jsonPath("$.ticketCreated").value(false));
    }

    @Test
    void createCommentRejectsBlankText() throws Exception {
        mockMvc.perform(post("/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"text":"","channel":"web"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listCommentsReturnsOk() throws Exception {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Hello");
        comment.setChannel("web");
        comment.setCreatedAt(Instant.parse("2026-07-24T12:00:00Z"));

        when(commentService.findAll()).thenReturn(List.of(comment));

        mockMvc.perform(get("/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Hello"));
    }
}
