package com.roome.domain.cd.entity;

import com.roome.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cd")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cd extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title; // CD 제목

  @Column(nullable = false)
  private String artist; // 가수

  @Column(nullable = false)
  private String album; // 앨범명

  @Column(nullable = false)
  private String genre; // 장르

  @Column(nullable = false)
  private String coverUrl; // 앨범 커버 이미지 URL

  @Column(nullable = false, unique = true)
  private String youtubeVideoId; // 유튜브 영상 ID

  @Column(nullable = false)
  private int duration; // 영상 길이 (초 단위)

  public Cd(String title, String artist, String album, String genre, String coverUrl,
      String youtubeVideoId, int duration) {
    this.title = title;
    this.artist = artist;
    this.album = album;
    this.genre = genre;
    this.coverUrl = coverUrl;
    this.youtubeVideoId = youtubeVideoId;
    this.duration = duration;
  }
}
