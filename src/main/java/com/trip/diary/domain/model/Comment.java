package com.trip.diary.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

import static com.trip.diary.domain.constants.Constants.UNIDENTIFIED_MEMBER_ID;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", referencedColumnName = "id")
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

    public void delete() {
        this.content = "삭제된 댓글입니다.";
        this.member = Member.builder().id(UNIDENTIFIED_MEMBER_ID).build();
        this.deletedAt = LocalDateTime.now();
    }
}
