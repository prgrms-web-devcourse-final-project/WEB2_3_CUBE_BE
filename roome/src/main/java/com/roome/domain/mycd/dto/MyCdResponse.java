package com.roome.domain.mycd.dto;

import com.roome.domain.mycd.entity.MyCd;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class MyCdResponse {
  private Long myCdId;
  private Long cdId;
  private String title;
  private String artist;
  private String album;
  private List<String> genres;
  private String coverUrl;
  private String youtubeUrl;
  private long duration;

  public static MyCdResponse fromEntity(MyCd myCd) {
    return MyCdResponse.builder()
        .myCdId(myCd.getId())
        .cdId(myCd.getCd().getId())
        .title(myCd.getCd().getTitle())
        .artist(myCd.getCd().getArtist())
        .album(myCd.getCd().getAlbum())
        .genres(myCd.getCd().getGenres())  // ✅ 이제 정상적으로 genres 가져올 수 있음
        .coverUrl(myCd.getCd().getCoverUrl())
        .youtubeUrl(myCd.getCd().getYoutubeUrl())
        .duration(myCd.getCd().getDuration())
        .build();
  }
}
