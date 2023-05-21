package com.trip.diary.domain.repository;

import com.trip.diary.client.RedisClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class PostLikeRedisRepository {
    private final RedisClient redisClient;
    private final static String POST_LIKE_KEY = "POST_LIKE";

    public void save(Long postId, Long userId) {
        redisClient.addValueToSet(POST_LIKE_KEY + postId, String.valueOf(userId));
    }

    public boolean existsByPostIdAndUserId(Long postId, Long userId) {
        return redisClient.isValueExistInSet(POST_LIKE_KEY + postId, String.valueOf(userId));
    }

    public void delete(Long postId, Long userId) {
        redisClient.removeValueToSet(POST_LIKE_KEY + postId, String.valueOf(userId));
    }

    public Long countByPostId(Long postId) {
        return redisClient.getSizeOfSet(POST_LIKE_KEY + postId);
    }
}
