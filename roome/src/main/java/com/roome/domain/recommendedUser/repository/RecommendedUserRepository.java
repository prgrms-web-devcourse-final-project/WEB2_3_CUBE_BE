package com.roome.domain.recommendedUser.repository;

import com.roome.domain.recommendedUser.entity.RecommendedUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendedUserRepository extends JpaRepository<RecommendedUser, Long> {

  /// 특정 사용자의 추천 사용자 목록을 유사도 점수 내림차순으로 조회
  List<RecommendedUser> findByUserIdOrderBySimilarityScoreDesc(Long userId);

  /// 특정 사용자의 추천 사용자 목록을 유사도 점수 내림차순으로 상위 N개 조회
  List<RecommendedUser> findTop5ByUserIdOrderBySimilarityScoreDesc(Long userId);

  /// 특정 사용자의 모든 추천 관계 삭제
  @Modifying
  @Query("DELETE FROM RecommendedUser ru WHERE ru.user.id = :userId")
  void deleteAllByUserId(@Param("userId") Long userId);

  /// 특정 사용자에 대한 추천 사용자 존재 여부 확인
  boolean existsByUserId(Long userId);

  /// 다른 사용자가 나를 추천한 데이터 삭제
  @Modifying
  @Query("DELETE FROM RecommendedUser ru WHERE ru.recommendedUser.id = :userId")
  void deleteAllByRecommendedUserId(@Param("userId") Long userId);
}
