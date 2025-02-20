package com.roome.domain.cdcomment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CdCommentListResponse {
  private List<CdCommentResponse> data;  // 댓글 리스트
  private int page;  // 현재 페이지
  private int pageSize;  // 한 페이지 당 댓글 개수 (디폴트: 5)
  private long totalElements;  // 전체 댓글 개수
  private int totalPages;  // 전체 페이지 개수
}
