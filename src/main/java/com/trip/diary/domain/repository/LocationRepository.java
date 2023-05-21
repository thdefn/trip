package com.trip.diary.domain.repository;

import com.trip.diary.domain.model.Location;
import com.trip.diary.domain.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findFirstByTripOrderByIdDesc(Trip trip);

    List<Location> findByTripOrderByIdDesc(Trip trip);
}
