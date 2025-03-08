package com.roome.domain.mybook.entity.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class MyBookCountRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "mybook::user::%s::count";

    public void createOrUpdate(Long roomOwner, Long count, Duration ttl) {
        redisTemplate.opsForValue().set(generateKey(roomOwner), String.valueOf(count), ttl);
    }

    public Long read(Long roomOwnerId) {
        String result = redisTemplate.opsForValue().get(generateKey(roomOwnerId));
        return result == null ? null : Long.parseLong(result);
    }

    private String generateKey(Long roomOwnerId) {
        return KEY_FORMAT.formatted(roomOwnerId);
    }
}
