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
import com.trip.diary.exception.ErrorCode;
import com.trip.diary.exception.PostException;
import com.trip.diary.exception.TripException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @InjectMocks
    private CommentService commentService;

    Member member = Member.builder()
            .id(1L)
            .username("qwerty99")
            .nickname("김맹맹")
            .password("1234567")
            .profilePath(null)
            .phone("01011111111")
            .build();

    Member participant1 = Member.builder()
            .id(2L)
            .username("abcde")
            .nickname("하이")
            .profilePath(null)
            .build();

    Trip trip = Trip.builder()
            .id(1L)
            .title("임의의 타이틀")
            .isPrivate(true)
            .description("임의의 설명")
            .build();

    Post post = Post.builder()
            .id(1L)
            .content("제주도 도착입니당")
            .member(member)
            .trip(trip)
            .build();

    @Test
    @DisplayName("댓글 생성 성공")
    void createTest_success() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("도착을 축하해요")
                .build();
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(post));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any()))
                .willReturn(true);
        given(commentRepository.save(any()))
                .willReturn(Comment.builder()
                        .id(1L)
                        .member(member)
                        .content("댓글")
                        .build());
        //when
        CommentDto result = commentService.create(1L, form, member);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        //then
        verify(commentRepository, times(1)).save(captor.capture());
        assertEquals(form.getContent(), captor.getValue().getContent());
        assertEquals("댓글", result.getContent());
        assertEquals(1L, result.getAuthorId());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 해당 기록 없음")
    void createTest_failWhenNotFoundPost() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("제주도 도착입니당")
                .build();

        given(postRepository.findById(any())).willReturn(Optional.empty());
        //when
        PostException exception = assertThrows(PostException.class, () -> commentService.create(1L, form, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 쓰기 권한 없음")
    void createTest_failWhenNotAuthorityWriteTrip() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("도착을 축하해요")
                .build();
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(post));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any()))
                .willReturn(false);
        //when
        TripException exception = assertThrows(TripException.class, () -> commentService.create(1L, form, member));
        //then
        assertEquals(ErrorCode.NOT_AUTHORITY_WRITE_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정 성공")
    void updateTest_success() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("도착이에요~.~")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .content("제주도 도착~")
                .member(member)
                .post(post)
                .build();

        given(commentRepository.findById(anyLong()))
                .willReturn(Optional.of(comment));

        given(commentRepository.save(any()))
                .willReturn(Comment.builder()
                        .id(1L)
                        .parentComment(comment)
                        .member(member)
                        .content("댓글")
                        .build());
        //when
        CommentDto result = commentService.update(1L, form, member);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        //then
        verify(commentRepository, times(1)).save(captor.capture());
        assertEquals(form.getContent(), captor.getValue().getContent());
        assertEquals("댓글", result.getContent());
        assertEquals(1L, result.getAuthorId());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 해당 댓글 없음")
    void updateTest_failWhenNotFoundComment() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("제주도 도착입니당")
                .build();

        given(commentRepository.findById(any())).willReturn(Optional.empty());
        //when
        CommentException exception = assertThrows(CommentException.class, () -> commentService.update(1L, form, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_COMMENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 수정 실패 - 댓글 작성자 아님")
    void updateTest_failWhenNotCommentOwner() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("도착이에요~.~")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .content("제주도 도착~")
                .member(participant1)
                .post(post)
                .build();

        given(commentRepository.findById(anyLong()))
                .willReturn(Optional.of(comment));
        //when
        CommentException exception = assertThrows(CommentException.class, () -> commentService.update(1L, form, member));
        //then
        assertEquals(ErrorCode.NOT_COMMENT_OWNER, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 생성 성공")
    void ReCommentTest_success() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("도착을 축하해요")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .content("제주도 도착입니당")
                .member(member)
                .post(post)
                .build();

        given(commentRepository.findById(anyLong()))
                .willReturn(Optional.of(comment));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any()))
                .willReturn(true);
        given(commentRepository.save(any()))
                .willReturn(Comment.builder()
                        .id(1L)
                        .parentComment(comment)
                        .member(member)
                        .content("댓글")
                        .build());
        //when
        CommentDto result = commentService.reComment(1L, form, member);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        //then
        verify(commentRepository, times(1)).save(captor.capture());
        assertEquals(form.getContent(), captor.getValue().getContent());
        assertEquals("댓글", result.getContent());
        assertEquals(1L, result.getAuthorId());
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 원 댓글 없음")
    void ReCommentTest_failWhenNotFoundPost() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("제주도 도착입니당")
                .build();

        given(commentRepository.findById(any())).willReturn(Optional.empty());
        //when
        CommentException exception = assertThrows(CommentException.class, () -> commentService.reComment(1L, form, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_COMMENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 쓰기 권한 없음")
    void ReCommentTest_failWhenNotAuthorityWriteTrip() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("도착을 축하해요")
                .build();

        Comment comment = Comment.builder()
                .id(1L)
                .content("제주도 도착입니당")
                .member(member)
                .post(post)
                .build();

        given(commentRepository.findById(anyLong()))
                .willReturn(Optional.of(comment));

        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any()))
                .willReturn(false);
        //when
        TripException exception = assertThrows(TripException.class, () -> commentService.reComment(1L, form, member));
        //then
        assertEquals(ErrorCode.NOT_AUTHORITY_WRITE_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("대댓글 생성 실패 - 대댓글에 대댓글")
    void ReCommentTest_failWhenCanNotReCommentToReComment() {
        //given
        CreateCommentForm form = CreateCommentForm.builder()
                .content("도착을 축하해요")
                .build();

        Comment comment = Comment.builder()
                .id(2L)
                .content("제주도 도착입니당")
                .member(member)
                .parentComment(Comment.builder()
                        .id(1L)
                        .content("이곳은 제주도")
                        .member(participant1)
                        .build()
                )
                .post(post)
                .build();

        given(commentRepository.findById(anyLong()))
                .willReturn(Optional.of(comment));
        //when
        CommentException exception = assertThrows(CommentException.class, () -> commentService.reComment(1L, form, member));
        //then
        assertEquals(ErrorCode.CAN_NOT_RE_COMMENT_TO_RE_COMMENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 조회 성공")
    void readTest_success() {
        //given
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(post));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any()))
                .willReturn(true);
        given(commentRepository.findByPostAndParentCommentIsNull(any()))
                .willReturn(List.of(
                        Comment.builder()
                                .id(1L)
                                .content("제주도 도착입니당")
                                .member(member)
                                .post(post)
                                .reComments(
                                        List.of(
                                                Comment.builder()
                                                        .id(2L)
                                                        .content("저 너무 설레는데 어쩌죠 ?")
                                                        .member(participant1)
                                                        .post(post)
                                                        .build(),
                                                Comment.builder()
                                                        .id(3L)
                                                        .content("어디야 너ㅡㅡ")
                                                        .member(member)
                                                        .post(post)
                                                        .build()
                                        ))
                                .build(),
                        Comment.builder()
                                .id(4L)
                                .content("키킷,,, 나도 지금가는중임..")
                                .reComments(new ArrayList<>())
                                .member(participant1)
                                .post(post)
                                .build()
                ));
        //when
        List<CommentDto> result = commentService.read(1L, member);
        //then
        assertEquals(1L, result.get(0).getAuthorId());
        assertEquals(2, result.get(0).getCountOfComments());
        assertEquals("제주도 도착입니당", result.get(0).getContent());
        assertEquals(2L, result.get(0).getReComments().get(0).getId());
        assertEquals(2L, result.get(1).getAuthorId());
        assertEquals("키킷,,, 나도 지금가는중임..", result.get(1).getContent());
        assertEquals(0, result.get(1).getCountOfComments());
    }

    @Test
    @DisplayName("댓글 조회 실패 - 해당 기록 없음")
    void readTest_failWhenNotFoundPost() {
        //given
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        PostException exception = assertThrows(PostException.class, () -> commentService.read(1L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_POST, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 조회 실패 - 여행 기록장에 대한 읽기 권한 없음")
    void readTest_failWhenNotAuthorityReadTrip() {
        //given
        given(postRepository.findById(anyLong()))
                .willReturn(Optional.of(post));
        given(participantRepository.existsByTripAndMemberAndType(any(), any(), any()))
                .willReturn(false);
        //when
        TripException exception = assertThrows(TripException.class, () -> commentService.read(1L, member));
        //then
        assertEquals(ErrorCode.NOT_AUTHORITY_READ_TRIP, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 삭제 성공")
    void deleteTest_success() {
        //given
        given(commentRepository.findByIdAndDeletedAtIsNull(anyLong()))
                .willReturn(Optional.of(
                        Comment.builder()
                                .id(1L)
                                .content("제주도 도착입니당")
                                .member(member)
                                .post(post)
                                .reComments(new ArrayList<>())
                                .build()));
        //when
        commentService.delete(1L, member);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        //then
        verify(commentRepository, times(1)).delete(captor.capture());
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 대댓글이 있을 때")
    void deleteTest_successWhenCommentHaveReComment() {
        //given
        given(commentRepository.findByIdAndDeletedAtIsNull(anyLong()))
                .willReturn(Optional.of(
                        Comment.builder()
                                .id(1L)
                                .content("제주도 도착입니당")
                                .member(member)
                                .post(post)
                                .reComments(
                                        List.of(
                                                Comment.builder()
                                                        .id(2L)
                                                        .content("저 너무 설레는데 어쩌죠 ?")
                                                        .member(participant1)
                                                        .post(post)
                                                        .build(),
                                                Comment.builder()
                                                        .id(3L)
                                                        .content("어디야 너ㅡㅡ")
                                                        .member(member)
                                                        .post(post)
                                                        .build()
                                        ))
                                .build()));
        //when
        commentService.delete(1L, member);
        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        //then
        verify(commentRepository, times(1)).save(captor.capture());
        assertEquals("삭제된 댓글입니다.", captor.getValue().getContent());
        assertEquals(-1L, captor.getValue().getMember().getId());
        assertNotNull(captor.getValue().getDeletedAt());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 해당 댓글 없음")
    void deleteTest_failWhenNotFoundComment() {
        //given
        given(commentRepository.findByIdAndDeletedAtIsNull(anyLong()))
                .willReturn(Optional.empty());
        //when
        CommentException exception = assertThrows(CommentException.class, () -> commentService.delete(1L, member));
        //then
        assertEquals(ErrorCode.NOT_FOUND_COMMENT, exception.getErrorCode());
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 댓글 작성자 아님")
    void deleteTest_failWhenNotCommentOwner() {
        //given
        given(commentRepository.findByIdAndDeletedAtIsNull(anyLong()))
                .willReturn(Optional.of(
                        Comment.builder()
                                .id(1L)
                                .content("제주도 도착입니당")
                                .member(participant1)
                                .post(post)
                                .reComments(new ArrayList<>())
                                .build()));
        //when
        CommentException exception = assertThrows(CommentException.class, () -> commentService.delete(1L, member));
        //then
        assertEquals(ErrorCode.NOT_COMMENT_OWNER, exception.getErrorCode());
    }

}