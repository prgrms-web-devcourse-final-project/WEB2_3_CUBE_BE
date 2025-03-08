package com.roome.domain.mybookreview.entity.repository;

import com.roome.domain.mybookreview.entity.MyBookReviewQueryModel;
import com.roome.global.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MyBookReviewQueryModelRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "mybook::%s::review";

    public void create(Long myBookId, MyBookReviewQueryModel myBookReviewQueryModel, Duration ttl) {
        redisTemplate.opsForValue()
                .set(generateKey(myBookId), DataSerializer.serialize(myBookReviewQueryModel), ttl);
    }

    public Optional<MyBookReviewQueryModel> read(Long myBookId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(generateKey(myBookId))
        ).map(json -> DataSerializer.deserialize(json, MyBookReviewQueryModel.class));
    }

    public void update(Long myBookId, MyBookReviewQueryModel myBookReviewQueryModel) {
        redisTemplate.opsForValue()
                .setIfPresent(generateKey(myBookId), DataSerializer.serialize(myBookReviewQueryModel));
    }

    public void delete(Long myBookId) {
        redisTemplate.delete(generateKey(myBookId));
    }

    private String generateKey(Long myBookId) {
        return KEY_FORMAT.formatted(myBookId);
    }
}
