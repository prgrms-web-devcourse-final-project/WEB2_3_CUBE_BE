package com.roome.global.service;

import com.roome.global.exception.LockAcquisitionFailedException;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisLockService {

  private final RedissonClient redissonClient;

  public <T> T executeWithLock(String key, long waitTime, long leaseTime, Supplier<T> supplier) {
    RLock lock = redissonClient.getLock(key);
    boolean acquired = false;

    try {
      acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
      if (!acquired) {
        throw new LockAcquisitionFailedException();
      }
      return supplier.get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException("락 획득 중 인터럽트 발생", e);
    } finally {
      if (acquired && lock.isHeldByCurrentThread()) {
        lock.unlock();
      }
    }
  }
}
