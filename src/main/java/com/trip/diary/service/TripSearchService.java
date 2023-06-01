package com.trip.diary.service;

import com.trip.diary.client.ElasticSearchClient;
import com.trip.diary.domain.model.Member;
import com.trip.diary.domain.model.Trip;
import com.trip.diary.domain.repository.BookmarkRepository;
import com.trip.diary.dto.TripDto;
import com.trip.diary.elasticsearch.model.TripDocument;
import com.trip.diary.elasticsearch.repository.TripSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.trip.diary.domain.constants.Constants.SEARCH_PAGE_SIZE;

@Service
@RequiredArgsConstructor
public class TripSearchService {
    private final TripSearchRepository tripSearchRepository;
    private final BookmarkRepository bookmarkRepository;
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

    public Page<TripDto> search(int page, String keyword, Member member) {
        return tripSearchRepository
                .findByKeyword(keyword, keyword.replace(" ", ""),
                        PageRequest.of(page, SEARCH_PAGE_SIZE))
                .map(tripDocument -> TripDto.of(tripDocument,
                        bookmarkRepository.existsByTrip_IdAndMember(tripDocument.getId(), member)));
    }

    public Page<TripDto> searchByLocation(int page, String keyword, Member member) {
        return tripSearchRepository
                .findByLocationsName(keyword.replace(" ", ""), PageRequest.of(page, SEARCH_PAGE_SIZE))
                .map(tripDocument -> TripDto.of(tripDocument,
                        bookmarkRepository.existsByTrip_IdAndMember(tripDocument.getId(), member)));
    }
}
