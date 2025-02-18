package com.roome.domain.mycd.entity;

import com.roome.domain.cd.entity.Cd;
import com.roome.domain.room.entity.Room;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;

import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MyCd {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id")
  private Room room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cd_id")
  private Cd cd;

  public static MyCd create(User user, Room room, Cd cd) {
    return MyCd.builder()
        .user(user)
        .room(room)
        .cd(cd)
        .build();
  }
}
