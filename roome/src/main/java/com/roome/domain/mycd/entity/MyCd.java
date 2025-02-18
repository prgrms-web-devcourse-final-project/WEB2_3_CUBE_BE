package com.roome.domain.mycd.entity;

import com.roome.domain.cd.entity.Cd;
import com.roome.domain.room.entity.Room;
import com.roome.domain.user.entity.User;
import com.roome.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "my_cd")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyCd extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // CD를 추가한 사용자

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  private Room room; // CD가 추가된 방

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cd_id", nullable = false)
  private Cd cd; // 추가한 CD

  public MyCd(User user, Room room, Cd cd) {
    this.user = user;
    this.room = room;
    this.cd = cd;
  }
}
