package com.roome.domain.room.entity;

import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.furniture.exception.BookshelfFullException;
import com.roome.domain.room.exception.RoomAuthorizationException;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "room")
public class Room {

  @Id
  private Long id; // userId와 동일한 값

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId          // roomId = userId
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private RoomTheme theme = RoomTheme.BASIC;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Setter
  @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Furniture> furnitures = new ArrayList<>();

  @Version
  private Integer version;
  
  public static Room init(User user, LocalDateTime now) {
    Room room = new Room();
    room.user = user;
    room.theme = RoomTheme.BASIC;
    room.createdAt = now;
    room.furnitures.addAll(Furniture.createDefaultFurnitures(room, now));
    return room;
  }

  public int getMaxMusic() {
    return furnitures.stream()
        .filter(f -> f.getFurnitureType() == FurnitureType.CD_RACK)
        .mapToInt(Furniture::getMaxCapacity)
        .sum();
  }

  public int getMaxBooks() {
    return furnitures.stream()
        .filter(f -> f.getFurnitureType() == FurnitureType.BOOKSHELF)
        .mapToInt(Furniture::getMaxCapacity)
        .sum();
  }

  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  public void updateTheme(RoomTheme theme) {
    this.theme = theme;
  }

  public void validateOwner(Long userId) {
    if (user == null || !user.getId().equals(userId)) {
      throw new RoomAuthorizationException();
    }
  }

  public void checkBookshelfIsFull(Long myBookCount) {
    if (getMaxBooks() == myBookCount) {
      throw new BookshelfFullException();
    }
  }

  public void setFurnitures(List<Furniture> furnitures) {
    this.furnitures = furnitures;
  }

  public static Room createRoom(User user, RoomTheme theme) {
    Room room = new Room();
    room.id = user.getId();  // userId와 동일한 값 설정
    room.user = user;
    room.theme = theme;
    room.furnitures = new ArrayList<>();
    return room;
  }
}
