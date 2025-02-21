package com.roome.domain.mycd.entity;

import com.roome.domain.cd.entity.Cd;
import com.roome.domain.room.entity.Room;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyCd {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cd_id", nullable = false)
  private Cd cd;

  public static MyCd create(User user, Room room, Cd cd) {
    return MyCd.builder()
        .user(user)
        .room(room)
        .cd(cd)
        .build();
  }

  public void validateOwner(Long userId) {
    if (user == null || !user.getId().equals(userId)) {
      throw new IllegalArgumentException("해당 CD의 소유자가 아닙니다. userId: " + userId);
    }
  }
}
