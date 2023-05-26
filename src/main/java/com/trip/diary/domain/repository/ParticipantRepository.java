package com.trip.diary.domain.repository;

import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Participant;
import com.trip.diary.domain.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    boolean existsByTripAndMember(Trip trip, Member member);

    boolean existsByTripAndMemberAndType(Trip trip, Member member, ParticipantType type);

    Optional<Participant> findByTripAndMember_Id(Trip trip, Long memberId);

    List<Participant> findByMemberAndType(Member member, ParticipantType type);
}
