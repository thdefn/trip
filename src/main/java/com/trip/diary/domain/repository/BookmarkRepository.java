package com.trip.diary.domain.repository;

import com.trip.diary.domain.model.Bookmark;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Trip;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByTripAndMember(Trip trip, Member member);

    Slice<Bookmark> findByMember(Member member, Pageable pageable);
}
