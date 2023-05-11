package com.trip.diary.domain.model;

import com.trip.diary.dto.UpdateTripForm;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Trip extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String description;

    private LocalDateTime deletedAt;

    private boolean isPrivate;

    @ManyToOne
    @JoinColumn(name = "leader_id")
    private Member leader;

    @Builder.Default
    @OneToMany(mappedBy = "trip")
    private List<Participant> participants = new ArrayList<>();

    public void update(UpdateTripForm form){
        this.isPrivate = form.isPrivate();
        this.title = form.getTitle();
        this.description = form.getDescription();
    }
}
