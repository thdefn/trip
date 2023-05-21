package com.trip.diary.elasticsearch.model;

import com.trip.diary.domain.model.Member;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Document(indexName = "members")
public class MemberDocument {
    @Id
    private Long id;

    private String nickname;

    private String profileUrl;

    @Field(type = FieldType.Nested)
    @Builder.Default
    private List<Trip> trips = new ArrayList<>();

    @AllArgsConstructor
    @Getter
    public static class Trip {
        @Field(type = FieldType.Long)
        private Long id;
    }


    public static MemberDocument of(Member member) {
        return MemberDocument.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileUrl(member.getProfilePath())
                .build();
    }

    public void addTripId(Long tripId) {
        trips.add(new Trip(tripId));
    }

    public void removeTripId(Long tripId) {
        trips.stream()
                .filter(trip -> trip.getId().equals(tripId))
                .findAny()
                .ifPresent(trip -> trips.remove(trip));
    }

    public boolean isInvitedInTrip(Long tripId) {
        return trips.stream()
                .anyMatch(trip -> trip.getId().equals(tripId));
    }
}
