package com.roome.domain.mycd.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MyCdCreateRequest {

  @NotEmpty
  private String title;

  @NotEmpty
  private String artist;

  @NotEmpty
  private String album;

  @NotEmpty
  private List<String> genres;

  @NotEmpty
  private String coverUrl;

  @NotEmpty
  private String youtubeUrl;

  private long duration;

}
