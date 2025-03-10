package com.roome.domain.mycd.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MyCdCreateRequest {

  @NotEmpty
  private String title;

  @NotEmpty
  private String artist;

  @NotEmpty
  private String album;

  @NotNull
  private LocalDate releaseDate;

  private List<String> genres = new ArrayList<>();

  @NotEmpty
  private String coverUrl;

  @NotEmpty
  private String youtubeUrl;

  private long duration;

}
