package com.roome.global.config;

import com.roome.domain.recommendedUser.service.RecommendedUserService;
import com.roome.domain.user.entity.User;
import com.roome.domain.user.repository.UserRepository;
import com.roome.domain.user.service.UserService;
import com.roome.domain.userGenrePreference.service.GenrePreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 애플리케이션 시작 시 초기 데이터를 로드하는 클래스
 * 서버 재시작 시 추천 목록을 갱신하기 위한 용도
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements ApplicationRunner {

    private final RecommendedUserService recommendedUserService;
    private final GenrePreferenceService genrePreferenceService;
    private final UserRepository userRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("앱 시작시에 추천 유저를 생성하기 위해 실행되는 코드");
        try {
            updateAllUsersGenrePreferences();
            log.info("모든 사용자의 장르 선호도 업데이트에 성공했습니다.");

            recommendedUserService.updateAllRecommendations();
            log.info("추천 유저 생성에 성공했습니다.");
        } catch (Exception e) {
            log.error("추천 유저 생성에 실패했습니다.", e);
            // 애플리케이션 동작에 치명적이지 않으므로 예외를 던지지 않고 로그만 남김
        }
    }

    /**
     * 모든 사용자의 장르 선호도를 업데이트하는 메소드
     */
    private void updateAllUsersGenrePreferences() {
        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            try {
                // CD 장르 선호도 업데이트
                genrePreferenceService.updateCdGenrePreferences(user.getId());

                // 책 장르 선호도 업데이트
                genrePreferenceService.updateBookGenrePreferences(user.getId());
            } catch (Exception e) {
                log.error("사용자 ID {} 의 장르 선호도 업데이트 실패", user.getId(), e);
                // 한 사용자의 실패가 전체 프로세스를 중단하지 않도록 예외 처리
            }
        }
    }
}