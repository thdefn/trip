package com.trip.diary.domain.model;

import com.trip.diary.dto.CreatePostForm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @OneToMany(mappedBy = "post")
    private List<PostImage> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @ManyToOne
    private Member member;

    @ManyToOne
    private Location location;

    private LocalDateTime deletedAt;

    public void setContent(String content) {
        this.content = content;
    }

    public static Post of(CreatePostForm form, Location location, Trip trip, Member member){
        return Post.builder()
                .content(form.getContent())
                .member(member)
                .location(location)
                .trip(trip)
                .build();
    }
}
