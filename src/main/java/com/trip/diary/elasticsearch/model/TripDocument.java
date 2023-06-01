package com.trip.diary.elasticsearch.model;

import com.trip.diary.domain.model.Trip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "trips")
public class TripDocument extends BaseDocument {
    private String title;

    private String description;

    @Field(type = FieldType.Boolean)
    private boolean isPrivate;

    @Field(type = FieldType.Nested)
    @Builder.Default
    private List<Location> locations = new ArrayList<>();

    @AllArgsConstructor
    @Getter
    private static class Location {
        @Field(type = FieldType.Keyword)
        private String name;
    }

    public static TripDocument from(Trip trip) {
        return TripDocument.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .isPrivate(trip.isPrivate())
                .description(trip.getDescription())
                .build();
    }

    public void addLocation(String locationName) {
        locations.add(new Location(locationName));
    }

    public void removeLocation(String locationName) {
        locations.stream()
                .filter(location -> location.getName().equals(locationName))
                .findFirst()
                .ifPresent(location -> locations.remove(location));
    }

    public void update(Trip trip) {
        this.title = trip.getTitle();
        this.description = trip.getDescription();
        this.isPrivate = trip.isPrivate();
    }
}
