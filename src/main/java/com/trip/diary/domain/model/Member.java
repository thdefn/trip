package com.trip.diary.domain.model;

import com.trip.diary.domain.constants.MemberType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Member extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String phone;

    @Column(nullable = false, unique = true)
    private String nickname;

    private String profilePath;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private MemberType type = MemberType.USER;

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isReader(Long readerId) {
        return readerId.equals(this.id);
    }
}
