package com.roome.domain.rank.entity;

public enum TaskStatus {
  PENDING,  // 대기 중
  PROCESSING,  // 처리 중
  COMPLETED,  // 완료
  FAILED,  // 실패
  ABANDONED  // 여러 번 재시도 후 포기
}