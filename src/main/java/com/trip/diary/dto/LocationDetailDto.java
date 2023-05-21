package com.trip.diary.dto;

import com.trip.diary.domain.model.Location;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LocationDetailDto {
    private Long id;
    private String name;
    @Builder.Default
    private Set<String> profilePaths = new HashSet<>();
    @Builder.Default
    private List<PostDto> posts = new ArrayList<>();

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class PostDto {
        private Long id;
        private String imagePath;
    }

    public static LocationDetailDto of(Location location) {
        LocationDetailDto dto = LocationDetailDto.builder()
                .id(location.getId())
                .name(location.getName())
                .build();
        location.getPosts()
                .forEach(post -> {
                    dto.posts.addAll(
                            post.getImages().stream()
                            .map(image ->
                                    new PostDto(post.getId(), image.getImagePath()))
                                    .toList());
                    dto.profilePaths.add(post.getMember().getProfilePath());
                });
        return dto;
    }
}
