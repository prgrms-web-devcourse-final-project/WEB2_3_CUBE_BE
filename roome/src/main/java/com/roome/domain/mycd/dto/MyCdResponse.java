package com.roome.domain.mycd.dto;

import com.roome.domain.mycd.entity.MyCd;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyCdResponse {
  private Long myCdId;
  private Long cdId;
  private String title;
  private String artist;
  private String album;
  private String genre;
  private String coverUrl;
  private String youtubeUrl;
  private int duration;

  public static MyCdResponse fromEntity(MyCd myCd) {
    return MyCdResponse.builder()
        .myCdId(myCd.getId())
        .cdId(myCd.getCd().getId())
        .title(myCd.getCd().getTitle())
        .artist(myCd.getCd().getArtist())
        .album(myCd.getCd().getAlbum())
        .genre(myCd.getCd().getGenre())
        .coverUrl(myCd.getCd().getCoverUrl())
        .youtubeUrl(myCd.getCd().getYoutubeUrl()) // youtubeVideoId를 URL로 변환
        .duration(myCd.getCd().getDuration())
        .build();
  }
}
