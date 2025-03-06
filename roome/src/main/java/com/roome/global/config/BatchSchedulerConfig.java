package com.roome.global.config;

import com.roome.domain.recommendedUser.service.RecommendedUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchSchedulerConfig {

    private final RecommendedUserService recommendedUserService;

    /// 매일 새벽 3시에 모든 사용자의 추천 목록을 갱신하는 배치 작업
    /// 현재는 임시적으로 30분 처리.
    /// cron = "초 분 시 일 월 요일"
    @Scheduled(cron = "0 0/30 * * * ?")
    public void updateAllUserRecommendations() {
        log.info("Starting scheduled batch job: updating all user recommendations");
        try {
            recommendedUserService.updateAllRecommendations();
            log.info("Scheduled batch job completed successfully");
        } catch (Exception e) {
            log.error("Error during scheduled batch job", e);
        }
    }


}
