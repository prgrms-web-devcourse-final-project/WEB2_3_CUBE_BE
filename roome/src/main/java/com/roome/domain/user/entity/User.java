package com.roome.domain.user.entity;

import com.roome.domain.point.entity.Point;
import com.roome.domain.rank.entity.UserActivity;
import com.roome.domain.room.entity.Room;
import com.roome.domain.room.exception.RoomAuthorizationException;
import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

  @Column(nullable = false, length = 255)
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

  // TODO: 추후 Redis 사용
  @Column(length = 1000)
  private String refreshToken;

  @OneToOne(mappedBy = "user")
  @PrimaryKeyJoinColumn
  private Room room;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private Point point;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<UserActivity> userActivities = new ArrayList<>();

  public static User createWithoutRelations(String name, String nickname, String email,
      String profileImage, Provider provider, String providerId, Status status) {
    User user = new User();
    user.name = name;
    user.nickname = nickname;
    user.email = email;
    user.status = status;
    user.profileImage = profileImage;
    user.provider = provider;
    user.providerId = providerId;
    return user;
  }

  public static User create(String name, String nickname, String email, String profileImage,
      Provider provider, String providerId, LocalDateTime now) {
    User user = createWithoutRelations(name, nickname, email, profileImage, provider, providerId,
        Status.ONLINE);
    user.lastLogin = now;
    // Room과 Point는 외부에서 생성하고 설정
    return user;
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

  public boolean isAttendanceToday(LocalDateTime now) {
    LocalDateTime midnight = now.with(LocalTime.MIDNIGHT);
    return lastLogin != null && (lastLogin.isEqual(midnight) || lastLogin.isAfter(midnight));
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

  public void updateLastLogin(LocalDateTime now) {
    this.lastLogin = now;
  }
}