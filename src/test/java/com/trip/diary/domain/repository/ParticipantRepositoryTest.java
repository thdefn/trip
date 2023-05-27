package com.trip.diary.domain.repository;

import com.trip.diary.TripApplication;
import com.trip.diary.domain.constants.ParticipantType;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Trip;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = TripApplication.class)
class ParticipantRepositoryTest {
    @Autowired
    CacheManager cacheManager;

    @Autowired
    ParticipantRepository participantRepository;

    Member member = Member.builder()
            .id(1L)
            .build();

    Trip trip = Trip.builder()
            .id(1L)
            .build();

    private Optional<Boolean> getCachedData(Member member, Trip trip) {
        return ofNullable(cacheManager.getCache("TripAuthorities"))
                .map(cache -> cache.get(List.of(trip.getId(), member.getId()), Boolean.class));
    }

    @Test
    @DisplayName("여행 기록장 참여 여부 캐싱 성공")
    void cachedTest_success() {
        boolean result = participantRepository.existsByTripAndMemberAndType(trip, member, ParticipantType.ACCEPTED);

        assertEquals(result, getCachedData(member, trip).get());
    }
}