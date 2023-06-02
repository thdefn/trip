package com.trip.diary.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.trip.diary.domain.model.Bookmark;
import com.trip.diary.dto.TripBookmarkDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.trip.diary.domain.model.QBookmark.bookmark;
import static com.trip.diary.domain.model.QTrip.trip;
import static org.springframework.util.StringUtils.hasText;

@Repository
@RequiredArgsConstructor
public class TripRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    public Page<TripBookmarkDto> findByKeywordContainsOrderByBookmark(String keyword, Pageable pageable) {
        NumberPath<Long> aliasCount = Expressions.numberPath(Long.class, "countOfBookmarked");
        List<TripBookmarkDto> content = jpaQueryFactory.select(
                        Projections.constructor(TripBookmarkDto.class,
                                trip.id, trip.isPrivate,
                                trip.title, trip.description,
                                trip.id.count().as(aliasCount)))
                .from(bookmark)
                .leftJoin(bookmark.trip, trip)
                .where(
                        isTripPublic().andAnyOf(tripDescriptionContains(keyword), triTitleContains(keyword))
                )
                .groupBy(trip.id)
                .orderBy(aliasCount.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Bookmark> countQuery = jpaQueryFactory.selectFrom(bookmark)
                .leftJoin(bookmark.trip, trip)
                .where(
                        isTripPublic().andAnyOf(tripDescriptionContains(keyword), triTitleContains(keyword))
                );

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetch().size());


    }

    private BooleanExpression tripDescriptionContains(String keyword) {
        return hasText(keyword) ? trip.description.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression triTitleContains(String keyword) {
        return hasText(keyword) ? trip.title.containsIgnoreCase(keyword) : null;
    }

    private BooleanExpression isTripPublic() {
        return trip.isPrivate.isFalse();
    }


}
