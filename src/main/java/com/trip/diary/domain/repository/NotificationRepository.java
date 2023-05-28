package com.trip.diary.domain.repository;

import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByMember(Member member);

    Optional<Notification> findByIdAndMemberAndReadAtIsNull(Long id, Member member);

    Optional<Notification> findByIdAndMember(Long id, Member member);

    List<Notification> findByMemberAndReadAtIsNull(Member member);

    List<Notification> findByIdIn(Set<Long> ids);
}
