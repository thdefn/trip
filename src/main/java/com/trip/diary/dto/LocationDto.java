package com.trip.diary.dto;

import com.trip.diary.domain.model.Location;
import com.trip.diary.domain.model.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class LocationDto {
    private Long id;
    private String name;
    private String thumbnailPath;
    private int numbersOfImages;

    public static LocationDto of(Location location) {
        return LocationDto.builder()
                .id(location.getId())
                .name(location.getName())
                .thumbnailPath(location.getThumbnailPath())
                .numbersOfImages(
                        location.getPosts().stream()
                                .map(post -> post.getImages().size())
                                .reduce(0, Integer::sum)
                        )
                .build();
    }
}
