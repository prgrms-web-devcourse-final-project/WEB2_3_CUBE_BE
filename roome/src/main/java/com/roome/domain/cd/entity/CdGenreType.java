package com.roome.domain.cd.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "cd_genre_type")
public class CdGenreType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String name;

  public static CdGenreType create(String name) {
    return CdGenreType.builder().name(name).build();
  }

  public CdGenreType(String name) {
    this.name = name;
  }
}

