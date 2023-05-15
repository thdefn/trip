package com.trip.diary.elasticsearch.repository;

import com.trip.diary.elasticsearch.model.MemberDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MemberSearchRepository extends ElasticsearchRepository<MemberDocument, Long> {
    List<MemberDocument> findByIdIn(Set<Long> ids);
}
