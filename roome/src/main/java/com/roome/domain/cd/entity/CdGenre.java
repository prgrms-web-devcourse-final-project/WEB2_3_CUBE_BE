package com.roome.domain.cd.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CdGenre {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cd_id", nullable = false)
  private Cd cd;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cd_genre_type_id", nullable = false)
  private CdGenreType genreType;

  public CdGenre(Cd cd, CdGenreType genreType) {
    this.cd = cd;
    this.genreType = genreType;
  }
}

