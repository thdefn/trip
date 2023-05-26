package com.trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trip.diary.domain.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
@Getter
public class CommentDto {
    private Long id;
    private String content;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer countOfComments;
    private long countOfLikes;
    private Long authorId;
    private String authorNickname;
    private String authorProfilePath;
    private Boolean isReader;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isReaderLiked;
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
                .build();
    }

    public static CommentDto of(Comment comment, List<CommentDto> reCommentDtos,
                                Long readerId, Long countOfLikes, boolean isReaderLiked) {
        return CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .countOfLikes(countOfLikes)
                .countOfComments(comment.getReComments().size())
                .reComments(reCommentDtos)
                .authorId(comment.getMember().getId())
                .authorNickname(comment.getMember().getNickname())
                .authorProfilePath(comment.getMember().getProfilePath())
                .isReader(comment.getMember().isReader(readerId))
                .isReaderLiked(isReaderLiked)
                .build();
    }
}
