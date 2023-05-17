package com.trip.diary;

import com.trip.diary.elasticsearch.model.MemberDocument;
import com.trip.diary.elasticsearch.repository.MemberSearchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest
class TripApplicationTests {

//    @Autowired
//    private MemberSearchRepository memberSearchRepository;

//    @Autowired
//    private ElasticsearchOperations elasticsearchOperations;

    @Test
    void contextLoads() {
    }

//    @Test
//    void test() {
//        //given
//        Page<MemberDocument> memberDocuments = memberSearchRepository.findAll(Pageable.ofSize(5));
//        //when
//        memberDocuments.getContent().stream()
//                .forEach(memberDocument -> {
//                    System.out.println(memberDocument.getId());
//                    System.out.println("trip-->" + memberDocument.getTrips());
//                });
//        //then
//    }
//
//    @Test
//    void test2() {
//        //given
//        List<MemberDocument> memberDocuments = memberSearchRepository.findByIdIn(Set.of(3L,5L,6L,8L));
//        //when
//        memberDocuments.stream()
//                .forEach(memberDocument -> {
//                    System.out.println(memberDocument.getId());
//                    System.out.println("trip-->" + memberDocument.getTrips().get(0).getId());
//                });
//        //then
//    }

//    @Test
//    void test3() {
//        List<UpdateQuery> updateQueries = memberSearchRepository.findByIdIn(Set.of(3L, 7L, 8L, 10L))
//                .stream().map(
//                        memberDocument ->
//                        {
//                            memberDocument.addTripId(4L);
//                            return UpdateQuery.builder(memberDocument.getId().toString())
//                                    .withDocument(elasticsearchOperations.getElasticsearchConverter().mapObject(memberDocument))
//                                    .withDocAsUpsert(true)
//                                    .build();
//                        }
//                ).collect(Collectors.toList());
//
//        //elasticsearchOperations.bulkUpdate(updateQueries, IndexCoordinates.of("members"));
//    }

}
