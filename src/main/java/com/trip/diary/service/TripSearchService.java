package com.trip.diary.service;

import com.trip.diary.client.ElasticSearchClient;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.elasticsearch.model.TripDocument;
import com.trip.diary.elasticsearch.repository.TripSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripSearchService {
    private final TripSearchRepository tripSearchRepository;
    private final ElasticSearchClient elasticSearchClient;
    private static final String INDEX_NAME_OF_TRIP = "trips";

    public void save(Trip trip) {
        elasticSearchClient.save(TripDocument.from(trip));
    }

    public void update(Trip trip) {
        tripSearchRepository.findById(trip.getId())
                .ifPresent(tripDocument -> {
                            tripDocument.update(trip);
                            elasticSearchClient.update(INDEX_NAME_OF_TRIP, tripDocument);
                        }
                );
    }

    public void addLocationToTripDocument(Long tripId, String locationName) {
        tripSearchRepository.findById(tripId)
                .ifPresent(tripDocument -> {
                            tripDocument.addLocation(locationName);
                            elasticSearchClient.update(INDEX_NAME_OF_TRIP, tripDocument);
                        }
                );
    }

    public void removeToTripDocument(Long tripId, String locationName) {
        tripSearchRepository.findById(tripId)
                .ifPresent(tripDocument -> {
                            tripDocument.removeLocation(locationName);
                            elasticSearchClient.update(INDEX_NAME_OF_TRIP, tripDocument);
                        }
                );
    }
}
