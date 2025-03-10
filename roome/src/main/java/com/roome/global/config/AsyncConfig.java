package com.roome.global.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

// 비동기 처리를 위한 설정
@Configuration
@EnableAsync
public class AsyncConfig {

  // 알림 처리를 위한 Executor 설정
  @Bean(name = "notificationTaskExecutor")
  public Executor notificationTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(10);
    executor.setThreadNamePrefix("notification-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "taskExecutor")
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(5);
    executor.setQueueCapacity(10);
    executor.setThreadNamePrefix("genre-preference-");
    executor.initialize();
    return executor;
  }

  @Bean(name = "rankingTaskExecutor")
  public Executor rankingTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(20);
    executor.setMaxPoolSize(100);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("ranking-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }
}