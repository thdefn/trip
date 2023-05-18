package com.trip.diary.dto;

import com.trip.diary.domain.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class CreatePostDto {
    private Long id;
    private String content;
    private List<String> imagePaths;
    private Long locationId;
    private String locationName;

    public static CreatePostDto of(Post post, List<String> imagePaths) {
        return CreatePostDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .imagePaths(imagePaths)
                .locationId(post.getLocation().getId())
                .locationName(post.getLocation().getName())
                .build();
    }
}
