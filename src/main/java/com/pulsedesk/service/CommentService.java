package com.pulsedesk.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.pulsedesk.model.Comment;
import com.pulsedesk.model.CreateCommentRequest;
import com.pulsedesk.repository.CommentRepository;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository){
        this.commentRepository = commentRepository;
    }

    @Transactional
    public Comment create(CreateCommentRequest request) {

        Comment comment = new Comment();
        comment.setText(request.getText());
        comment.setChannel(request.getChannel());
        return commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<Comment> findAll() {
        return commentRepository.findAll();
    }
}