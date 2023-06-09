package com.trip.diary.domain.repository;

import com.trip.diary.domain.model.Comment;
import com.trip.diary.domain.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostAndParentCommentIsNull(Post post);

    Optional<Comment> findByIdAndDeletedAtIsNull(Long id);
}
