package com.trip.diary.dto;

import com.trip.diary.domain.model.Post;
import com.trip.diary.domain.model.PostImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PostDetailDto {
    private Long id;
    private String content;
    private List<String> imagePaths;
    private Long locationId;
    private String locationName;
    private Long authorId;
    private long countOfLikes;
    private String authorNickname;
    private String authorProfilePath;
    private Boolean isReader;
    private Boolean isReaderLiked;
    private LocalDate createdAt;

    public static PostDetailDto of(Post post, List<String> imagePaths, Long readerId) {
        boolean isReader = post.getMember().isReader(readerId);
        return PostDetailDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .imagePaths(imagePaths)
                .locationId(post.getLocation().getId())
                .locationName(post.getLocation().getName())
                .authorId(post.getMember().getId())
                .authorNickname(isReader ? post.getMember().getNickname() + "(나)"
                        : post.getMember().getNickname())
                .authorProfilePath(post.getMember().getProfilePath())
                .isReader(isReader)
                .createdAt(post.getCreatedAt().toLocalDate())
                .build();
    }

    public static PostDetailDto of(Post post, List<String> imagePaths,
                                   long countOfLikes, boolean isReaderLiked,
                                   Long readerId) {
        boolean isReader = post.getMember().isReader(readerId);
        return PostDetailDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .countOfLikes(countOfLikes)
                .imagePaths(imagePaths)
                .locationId(post.getLocation().getId())
                .locationName(post.getLocation().getName())
                .authorId(post.getMember().getId())
                .authorNickname(isReader ? post.getMember().getNickname() + "(나)"
                        : post.getMember().getNickname())
                .authorProfilePath(post.getMember().getProfilePath())
                .isReader(isReader)
                .isReaderLiked(isReaderLiked)
                .createdAt(post.getCreatedAt().toLocalDate())
                .build();
    }

    public static PostDetailDto of(Post post, long countOfLikes,
                                   boolean isReaderLiked, Long readerId) {
        boolean isReader = post.getMember().isReader(readerId);
        return PostDetailDto.builder()
                .id(post.getId())
                .content(post.getContent())
                .countOfLikes(countOfLikes)
                .imagePaths(post.getImages().stream()
                        .map(PostImage::getImagePath)
                        .collect(Collectors.toList()))
                .locationId(post.getLocation().getId())
                .locationName(post.getLocation().getName())
                .authorId(post.getMember().getId())
                .authorNickname(isReader ? post.getMember().getNickname() + "(나)"
                        : post.getMember().getNickname())
                .authorProfilePath(post.getMember().getProfilePath())
                .isReader(isReader)
                .isReaderLiked(isReaderLiked)
                .createdAt(post.getCreatedAt().toLocalDate())
                .build();
    }
}
