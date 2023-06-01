package com.trip.diary.elasticsearch.repository;

import com.trip.diary.elasticsearch.model.TripDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TripSearchRepository extends ElasticsearchRepository<TripDocument, Long> {
    Optional<TripDocument> findById(Long id);

    @Query("{\"bool\": { \"should\": [ \n" +
            "    {\"wildcard\": { \"title\": { \"value\": \"*?0*\"}}},\n" +
            "    {\"match_phrase\": {\"description\": \"?0\"}},\n" +
            "    {\"term\": {\"locations.name\": \"?1\"}}],\n" +
            "    \"filter\": [{\"term\": {\"isPrivate\": false}}], \"minimum_should_match\": 1}}")
    Page<TripDocument> findByKeyword(String keyword, String locationName, Pageable pageable);

    @Query("\"bool\": { \"must\": [ \n" +
            "    {\"term\": {\"locations.name\": \"?0\"}}],\n" +
            "    \"filter\": [{\"term\": {\"isPrivate\": false}}]}")
    Page<TripDocument> findByLocationsName(String locationName, Pageable pageable);
}
