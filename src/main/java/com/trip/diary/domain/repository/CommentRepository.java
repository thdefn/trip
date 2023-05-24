package com.trip.diary.domain.repository;

import com.trip.diary.domain.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
