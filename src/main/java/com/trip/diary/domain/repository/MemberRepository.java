package com.trip.diary.domain.repository;

import com.trip.diary.domain.model.Member;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByPhone(String phone);

    List<Member> findAllByIdIn(Set<Long> ids);

    Slice<Member> findByIdNot(Long id);
}
