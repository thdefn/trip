package com.trip.diary.domain.repository;

import com.trip.diary.client.RedisClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
@RequiredArgsConstructor
public class CommentLikeRedisRepository {
    private final RedisClient redisClient;
    private final static String COMMENT_LIKE_KEY = "COMMENT_LIKE";

    public void save(Long commentId, Long userId) {
        redisClient.addValueToSet(COMMENT_LIKE_KEY + commentId, String.valueOf(userId));
    }

    public boolean existsByCommentIdAndUserId(Long commentId, Long userId) {
        return redisClient.isValueExistInSet(COMMENT_LIKE_KEY + commentId, String.valueOf(userId));
    }

    public void delete(Long commentId, Long userId) {
        redisClient.removeValueToSet(COMMENT_LIKE_KEY + commentId, String.valueOf(userId));
    }

    public Long countByCommentId(Long commentId) {
        return redisClient.getSizeOfSet(COMMENT_LIKE_KEY + commentId);
    }

    public void deleteAllByCommentId(Long commentId) {
        redisClient.removeKeyToSet(COMMENT_LIKE_KEY + commentId);
    }
}
