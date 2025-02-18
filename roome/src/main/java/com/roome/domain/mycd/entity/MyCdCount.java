package com.roome.domain.mycd.entity;

import com.roome.domain.room.entity.Room;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "my_cd_count")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MyCdCount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long count;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  public static MyCdCount init(Room room) {
    return MyCdCount.builder()
        .count(1L)
        .room(room)
        .build();
  }

  public void increment() {
    this.count++;
  }

  public void decrement() {
    if (this.count > 0) {
      this.count--;
    }
  }
}
