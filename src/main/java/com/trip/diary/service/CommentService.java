package com.trip.diary.service;

import com.trip.diary.domain.model.Comment;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Post;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.CommentRepository;
import com.trip.diary.domain.repository.ParticipantRepository;
import com.trip.diary.domain.repository.PostRepository;
import com.trip.diary.dto.CommentDto;
import com.trip.diary.dto.CreateCommentForm;
import com.trip.diary.exception.CommentException;
import com.trip.diary.exception.PostException;
import com.trip.diary.exception.TripException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.trip.diary.domain.type.ParticipantType.ACCEPTED;
import static com.trip.diary.exception.ErrorCode.*;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ParticipantRepository participantRepository;

    @Transactional
    public CommentDto create(Long postId, CreateCommentForm form, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        validationMemberHaveWriteAuthority(post.getTrip(), member);

        return CommentDto.of(commentRepository.save(
                Comment.builder()
                        .content(form.getContent())
                        .post(post)
                        .member(member)
                        .build()));
    }

    private void validationMemberHaveWriteAuthority(Trip trip, Member member) {
        if (!participantRepository.existsByTripAndMemberAndType(trip, member, ACCEPTED)) {
            throw new TripException(NOT_AUTHORITY_WRITE_TRIP);
        }
    }

    public CommentDto update(Long commentId, CreateCommentForm form, Member member) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(NOT_FOUND_COMMENT));

        if (!Objects.equals(comment.getMember().getId(), member.getId())) {
            throw new CommentException(NOT_COMMENT_OWNER);
        }

        comment.modifyContent(form.getContent());

        return CommentDto.of(commentRepository.save(comment));
    }

    @Transactional
    public CommentDto ReComment(Long commentId, CreateCommentForm form, Member member) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(NOT_FOUND_COMMENT));

        validationMemberHaveWriteAuthority(comment.getPost().getTrip(), member);

        return CommentDto.of(commentRepository.save(
                Comment.builder()
                        .content(form.getContent())
                        .post(comment.getPost())
                        .parentComment(comment)
                        .member(member)
                        .build()));
    }

    @Transactional
    public List<CommentDto> read(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        validationMemberHaveReadAuthority(post.getTrip(), member);

        return commentRepository.findByPostAndParentCommentIsNull(post).stream()
                .map(comment -> CommentDto.of(comment, member.getId())).collect(Collectors.toList());
    }

    private void validationMemberHaveReadAuthority(Trip trip, Member member) {
        if (trip.isPrivate() && !participantRepository.existsByTripAndMemberAndType(trip, member, ACCEPTED)) {
            throw new TripException(NOT_AUTHORITY_READ_TRIP);
        }
    }
}
