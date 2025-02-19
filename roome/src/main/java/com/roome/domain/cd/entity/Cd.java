package com.roome.domain.cd.entity;

import com.roome.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.List;
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

  @ElementCollection
  @CollectionTable(name = "cd_genres", joinColumns = @JoinColumn(name = "cd_id"))
  @Column(name = "genre", nullable = false)
  private List<String> genres; // 장르

  @Column(nullable = false)
  private String coverUrl; // 앨범 커버 이미지 URL

  @Column(nullable = false, unique = true)
  private String youtubeUrl;

  @Column(nullable = false)
  private long duration;

  public Cd(String title, String artist, String album, List<String> genres, String coverUrl,
      String youtubeUrl, int duration) {
    this.title = title;
    this.artist = artist;
    this.album = album;
    this.genres = genres;
    this.coverUrl = coverUrl;
    this.youtubeUrl = youtubeUrl;
    this.duration = duration;
  }
}
