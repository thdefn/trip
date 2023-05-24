package com.trip.diary.domain.model;

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
public class Comment extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment")
    private List<Comment> reComments;

    @ManyToOne
    private Post post;

    @ManyToOne
    private Member member;

    private LocalDateTime deletedAt;

    public void modifyContent(String content) {
        this.content = content;
    }
}
