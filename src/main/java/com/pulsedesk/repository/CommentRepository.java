package com.pulsedesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pulsedesk.model.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}