package com.trip.diary.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.trip.diary.domain.model.QLocation.location;

@Repository
@RequiredArgsConstructor
public class LocationRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public List<String> findLocationNameByTripId(Long tripId) {
        return jpaQueryFactory.select(location.name)
                .from(location)
                .where(location.trip.id.eq(tripId))
                .fetch();
    }
}
