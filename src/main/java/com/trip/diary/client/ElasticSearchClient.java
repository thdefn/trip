package com.trip.diary.client;

import com.trip.diary.elasticsearch.model.BaseDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ElasticSearchClient {
    private final ElasticsearchOperations elasticsearchOperations;

    public <T extends BaseDocument> void save(T document) {
        elasticsearchOperations.save(document);
    }

    public <T extends BaseDocument> void update(String indexName, T document) {
        UpdateQuery updateQuery = UpdateQuery.builder(document.getId().toString())
                .withDocument(elasticsearchOperations.getElasticsearchConverter().mapObject(document))
                .withDocAsUpsert(true)
                .build();
        elasticsearchOperations.update(updateQuery, IndexCoordinates.of(indexName));
    }

    public <T extends BaseDocument> void update(String indexName, List<T> documents) {
        List<UpdateQuery> updateQueries = documents.stream()
                .map(document ->
                        UpdateQuery.builder(document.getId().toString())
                                .withDocument(elasticsearchOperations.getElasticsearchConverter().mapObject(document))
                                .withDocAsUpsert(true)
                                .build())
                .collect(Collectors.toList());
        elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of(indexName));
    }
}
