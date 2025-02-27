package com.roome.domain.user.entity;

import com.roome.domain.point.entity.Point;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.exception.RoomAuthorizationException;
import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255, unique = true)
  private String email;

  // 소셜에서 가져온 실제 이름
  @Column(nullable = false, length = 30)
  private String name;

  // 서비스에서 사용할 닉네임
  private String nickname;

  @Column(length = 500)
  private String profileImage;

  @Column(length = 101)
  private String bio;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Provider provider;

  @Column(length = 255, unique = true, nullable = false)
  private String providerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Status status;

  @Column(nullable = true)
  private LocalDateTime lastLogin;

  @Column(nullable = true) 
  private LocalDateTime lastGuestbookReward;

  // TODO: 추후 Redis 사용
  @Column(length = 1000)
  private String refreshToken;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Room room;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Point point;

  @PrePersist
  public void prePersist() {
    if (this.lastLogin == null) {
      this.lastLogin = LocalDateTime.now();
    }
    if (this.point == null) {
      this.point = new Point(this, 0, 0, 0);
    }
  }

  public void updateProfile(String nickname, String bio) {
    boolean updated = false;
    if (nickname != null && !nickname.equals(this.nickname)) {
      this.nickname = nickname;
      updated = true;
    }
    if (bio != null && !bio.equals(this.bio)) {
      this.bio = bio;
      updated = true;
    }
    if (updated) {
      this.lastLogin = LocalDateTime.now();
    }
  }

  public void updateProfile(String nickname, String profileImage, String bio) {
    boolean updated = false;

    if (nickname != null && !nickname.equals(this.nickname)) {
      this.nickname = nickname;
      updated = true;
    }

    if (profileImage != null && !profileImage.equals(this.profileImage)) {
      this.profileImage = profileImage;
      updated = true;
    }

    if (bio != null && !bio.equals(this.bio)) {
      this.bio = bio;
      updated = true;
    }

    if (updated) {
      this.lastLogin = LocalDateTime.now();
    }
  }

  public void updateStatus(Status status) {
    this.status = status;
  }

  public void updateRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public void updateProvider(Provider provider) {
    this.provider = provider;
  }

  public void updateProviderId(String providerId) {
    this.providerId = providerId;
  }

  public boolean isAttendanceToday(LocalDateTime now) {
    LocalDateTime midnight = now.with(LocalTime.MIDNIGHT);
    return midnight.isEqual(lastLogin) || midnight.isAfter(lastLogin);
  }

  public void accumulatePoints(int point) {
    this.point.addPoints(point);
  }

  public void payPoints(int point) {
    this.point.subtractPoints(point);
  }

  public void validateRoomOwner(Long roomId) {
    if (room == null || !room.getId().equals(roomId)) {
      throw new RoomAuthorizationException();
    }
  }

  public void updateLastGuestbookReward(LocalDateTime date) {
    this.lastGuestbookReward = date;
  }
}
