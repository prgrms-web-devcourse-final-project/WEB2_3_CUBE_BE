package com.roome.domain.mybook.entity.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MyBookIdsRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "mybook::user::%s::ids";

    public void add(Long roomOwnerId, Long myBookId, Duration ttl) {
        redisTemplate.opsForZSet().add(generateKey(roomOwnerId), String.valueOf(myBookId), 0);
        redisTemplate.expire(generateKey(roomOwnerId), ttl);
    }

    public List<Long> readAll(Long roomOwnerId, Long pageSize, Long lastMyBookId, String keyword) {
        Set<String> myBookIds = redisTemplate.opsForZSet().reverseRangeByLex(
                generateKey(roomOwnerId),
                lastMyBookId == null ?
                        Range.unbounded() :
                        Range.leftUnbounded(Range.Bound.exclusive(String.valueOf(lastMyBookId))),
                Limit.limit().count(pageSize.intValue())
        );
        if (keyword != null || myBookIds == null || myBookIds.isEmpty()) {
            return List.of();
        }
        return myBookIds.stream().map(Long::valueOf).toList();
    }
    
    public void delete(Long roomOwnerId, List<String> myBookIds) {
        redisTemplate.opsForZSet().remove(generateKey(roomOwnerId), myBookIds.toArray());
    }

    private String generateKey(Long roomOwnerId) {
        return KEY_FORMAT.formatted(roomOwnerId);
    }
}
