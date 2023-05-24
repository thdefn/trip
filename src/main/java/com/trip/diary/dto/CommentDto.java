package com.trip.diary.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.trip.diary.domain.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class CommentDto {
    private Long id;
    private String content;
    private long likeOfComments;
    private Long authorId;
    private String authorNickname;
    private String authorProfilePath;
    private Boolean isReader;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isReaderLiked;

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
}
