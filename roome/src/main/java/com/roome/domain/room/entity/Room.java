package com.roome.domain.room.entity;

import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.furniture.exception.BookshelfFullException;
import com.roome.domain.room.exception.RoomAuthorizationException;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "room")
public class Room {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private RoomTheme theme = RoomTheme.BASIC;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Setter
  @OneToMany(mappedBy = "room")
  @Builder.Default
  private List<Furniture> furnitures = new ArrayList<>();

  @Version
  @Column(nullable = false)
  private Integer version = 0;

  public static Room init(User user, LocalDateTime now) {
    Room room = new Room();
    room.user = user;
    room.theme = RoomTheme.BASIC;
    room.createdAt = now;
    room.furnitures.addAll(Furniture.createDefaultFurnitures(room, now));
    return room;
  }

  public int getMaxMusic() {
    return furnitures.stream().filter(f -> f.getFurnitureType() == FurnitureType.CD_RACK)
        .mapToInt(Furniture::getMaxCapacity).sum();
  }

  public int getMaxBooks() {
    return furnitures.stream().filter(f -> f.getFurnitureType() == FurnitureType.BOOKSHELF)
        .mapToInt(Furniture::getMaxCapacity).sum();
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
    room.user = user;
    room.theme = theme;
    room.furnitures = new ArrayList<>();
    return room;
  }
}
