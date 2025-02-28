package com.roome.domain.mycd.dto;

import com.roome.domain.mycd.entity.MyCd;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MyCdResponse {

  private Long myCdId;
  private String title;
  private String artist;
  private String album;
  private LocalDate releaseDate;
  private List<String> genres;
  private String coverUrl;
  private String youtubeUrl;
  private long duration;

  public static MyCdResponse fromEntity(MyCd myCd) {
    return MyCdResponse.builder()
        .myCdId(myCd.getId())
        .title(myCd.getCd().getTitle())
        .artist(myCd.getCd().getArtist())
        .album(myCd.getCd().getAlbum())
        .releaseDate(myCd.getCd().getReleaseDate())
        .genres(myCd.getCd().getGenres())
        .coverUrl(myCd.getCd().getCoverUrl())
        .youtubeUrl(myCd.getCd().getYoutubeUrl())
        .duration(myCd.getCd().getDuration())
        .build();
  }
}
