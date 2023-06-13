package com.trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trip.diary.domain.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDto {
    private Long id;
    private String content;
    private Integer countOfComments;
    private Long countOfLikes;
    private Long authorId;
    private String authorNickname;
    private String authorProfilePath;
    private Boolean isReader;
    private Boolean isReaderLiked;
    private LocalDate createdAt;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CommentDto> reComments;

    public static CommentDto of(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .authorId(comment.getMember().getId())
                .authorNickname(comment.getMember().getNickname())
                .authorProfilePath(comment.getMember().getProfilePath())
                .isReader(true)
                .createdAt(comment.getCreatedAt().toLocalDate())
                .build();
    }

    public static CommentDto of(Comment comment, Long readerId, Long countOfLikes, boolean isReaderLiked) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .countOfLikes(countOfLikes)
                .authorId(comment.getMember().getId())
                .authorNickname(comment.getMember().getNickname())
                .authorProfilePath(comment.getMember().getProfilePath())
                .isReader(comment.getMember().isReader(readerId))
                .isReaderLiked(isReaderLiked)
                .createdAt(comment.getCreatedAt().toLocalDate())
                .build();
    }

    public static CommentDto of(Comment comment, List<CommentDto> reCommentDtos,
                                Long readerId, Long countOfLikes, boolean isReaderLiked) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .countOfLikes(countOfLikes)
                .countOfComments(reCommentDtos.size())
                .reComments(reCommentDtos)
                .authorId(comment.getMember().getId())
                .authorNickname(comment.getMember().getNickname())
                .authorProfilePath(comment.getMember().getProfilePath())
                .isReader(comment.getMember().isReader(readerId))
                .isReaderLiked(isReaderLiked)
                .createdAt(comment.getCreatedAt().toLocalDate())
                .build();
    }

    public static CommentDto blind(List<CommentDto> reCommentDtos) {
        return CommentDto.builder()
                .authorNickname("알수없음")
                .content("삭제된 댓글입니다.")
                .reComments(reCommentDtos)
                .countOfComments(reCommentDtos.size())
                .build();
    }
}
