package com.trip.diary.service;

import com.trip.diary.domain.model.Comment;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Post;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.CommentLikeRedisRepository;
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

import static com.trip.diary.domain.constants.ParticipantType.ACCEPTED;
import static com.trip.diary.exception.ErrorCode.*;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ParticipantRepository participantRepository;
    private final CommentLikeRedisRepository commentLikeRedisRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentDto create(Long postId, CreateCommentForm form, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        validationMemberHaveWriteAuthority(post.getTrip(), member);

        sendNotificationToPostAuthor(post.getTrip().getTitle(), post.getLocation().getName(),
                postId, member, post.getMember().getId());
        return CommentDto.of(commentRepository.save(
                Comment.builder()
                        .content(form.getContent())
                        .post(post)
                        .member(member)
                        .build()));
    }

    private void sendNotificationToPostAuthor(String tripTitle, String locationName, Long postId,
                                              Member sender, Long receiverId) {
        if (!Objects.equals(sender.getId(), receiverId)) {
            notificationService.notifyComment(tripTitle, locationName, postId,
                    sender.getNickname(), receiverId);
        }
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
    public CommentDto reComment(Long commentId, CreateCommentForm form, Member member) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentException(NOT_FOUND_COMMENT));

        if (!Objects.isNull(comment.getParentComment())) {
            throw new CommentException(CAN_NOT_RE_COMMENT_TO_RE_COMMENT);
        }

        validationMemberHaveWriteAuthority(comment.getPost().getTrip(), member);
        sendNotificationToCommentWriter(comment.getPost().getTrip().getTitle(),
                comment.getContent(), comment.getPost().getId(), member, comment.getMember().getId());

        return CommentDto.of(commentRepository.save(
                Comment.builder()
                        .content(form.getContent())
                        .post(comment.getPost())
                        .parentComment(comment)
                        .member(member)
                        .build()));
    }

    private void sendNotificationToCommentWriter(String tripTitle, String commentContent, Long postId,
                                              Member sender, Long receiverId) {
        if (!Objects.equals(sender.getId(), receiverId)) {
            notificationService.notifyReComment(tripTitle, commentContent, postId,
                    sender.getNickname(), receiverId);
        }
    }

    @Transactional
    public List<CommentDto> read(Long postId, Member member) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(NOT_FOUND_POST));

        validationMemberHaveReadAuthority(post.getTrip(), member);

        return commentRepository.findByPostAndParentCommentIsNull(post).stream()
                .map(comment -> getCommentDto(comment, member.getId()))
                .collect(Collectors.toList());
    }

    private CommentDto getCommentDto(Comment comment, Long readerId) {
        return Objects.isNull(comment.getDeletedAt()) ?
                CommentDto.of(comment,
                        getReCommentDto(comment.getReComments(), readerId),
                        readerId,
                        commentLikeRedisRepository.countByCommentId(comment.getId()),
                        commentLikeRedisRepository.existsByCommentIdAndUserId(comment.getId(), readerId))
                : CommentDto.blind(getReCommentDto(comment.getReComments(), readerId));
    }

    private List<CommentDto> getReCommentDto(List<Comment> reComments, Long readerId) {
        return reComments.stream()
                .map(comment -> CommentDto.of(comment, readerId,
                        commentLikeRedisRepository.countByCommentId(comment.getId()),
                        commentLikeRedisRepository.existsByCommentIdAndUserId(comment.getId(), readerId)))
                .collect(Collectors.toList());
    }

    private void validationMemberHaveReadAuthority(Trip trip, Member member) {
        if (trip.isPrivate() && !participantRepository.existsByTripAndMemberAndType(trip, member, ACCEPTED)) {
            throw new TripException(NOT_AUTHORITY_READ_TRIP);
        }
    }

    @Transactional
    public void delete(Long commentId, Member member) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentException(NOT_FOUND_COMMENT));

        if (!Objects.equals(comment.getMember().getId(), member.getId())) {
            throw new CommentException(NOT_COMMENT_OWNER);
        }

        if (!comment.getReComments().isEmpty()) {
            comment.delete();
            commentRepository.save(comment);
        } else {
            commentRepository.delete(comment);
        }
        commentLikeRedisRepository.deleteAllByCommentId(commentId);
    }

    @Transactional
    public void like(Long commentId, Member member) {
        Comment comment = commentRepository.findByIdAndDeletedAtIsNull(commentId)
                .orElseThrow(() -> new CommentException(NOT_FOUND_COMMENT));

        validationMemberHaveWriteAuthority(comment.getPost().getTrip(), member);

        if (commentLikeRedisRepository.existsByCommentIdAndUserId(commentId, member.getId())) {
            commentLikeRedisRepository.delete(commentId, member.getId());
        } else {
            commentLikeRedisRepository.save(commentId, member.getId());
        }
    }
}
