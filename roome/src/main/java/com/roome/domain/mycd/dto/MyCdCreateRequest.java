package com.roome.domain.mycd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyCdCreateRequest {

  @NotNull
  private Long cdId;

  @NotBlank
  private String title;

  @NotBlank
  private String artist;

  @NotBlank
  private String album;

  @NotBlank
  private String genre;

  @NotBlank
  private String coverUrl;

  @NotBlank
  private String youtubeUrl;

  @NotNull
  private int duration;
}
