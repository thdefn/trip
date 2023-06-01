package com.trip.diary.elasticsearch.repository;

import com.trip.diary.elasticsearch.model.TripDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripSearchRepository extends ElasticsearchRepository<TripDocument, Long> {
    Optional<TripDocument> findById(Long id);
}
