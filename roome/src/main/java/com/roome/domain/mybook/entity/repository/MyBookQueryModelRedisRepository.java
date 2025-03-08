package com.roome.domain.mybook.entity.repository;

import com.roome.domain.mybook.entity.MyBookQueryModel;
import com.roome.global.dataserializer.DataSerializer;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.function.Function.*;
import static java.util.stream.Collectors.*;


@Repository
@RequiredArgsConstructor
public class MyBookQueryModelRedisRepository {

    private final StringRedisTemplate redisTemplate;

    private static final String KEY_FORMAT = "mybook::%s";

    public void create(MyBookQueryModel myBookQueryModel, Duration ttl) {
        redisTemplate.opsForValue()
                .set(generateKey(myBookQueryModel), DataSerializer.serialize(myBookQueryModel), ttl);
    }

    public Optional<MyBookQueryModel> read(Long myBookId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(generateKey(myBookId))
        ).map(json -> DataSerializer.deserialize(json, MyBookQueryModel.class));
    }

    public Map<Long, MyBookQueryModel> readAll(List<Long> myBookIds) {
        List<String> keys = myBookIds.stream().map(this::generateKey).toList();
        List<String> jsons = redisTemplate.opsForValue().multiGet(keys);
        if (jsons == null || jsons.isEmpty()) {
            return Map.of();
        }
        return jsons.stream()
                .filter(Objects::nonNull)
                .map(json -> DataSerializer.deserialize(json, MyBookQueryModel.class))
                .collect(toMap(MyBookQueryModel::getId, identity()));
    }

    public void delete(Long myBookId) {
        redisTemplate.delete(generateKey(myBookId));
    }

    private String generateKey(MyBookQueryModel myBookQueryModel) {
        return generateKey(myBookQueryModel.getId());
    }

    private String generateKey(Long myBookId) {
        return KEY_FORMAT.formatted(myBookId);
    }
}
