package com.roome.domain.furniture.entity;

import com.roome.domain.furniture.exception.BookshelfMaxLevelException;
import com.roome.domain.furniture.exception.BookshelfUpgradeDenyException;
import com.roome.domain.room.entity.Room;
import com.roome.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "furniture")
public class Furniture {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private Room room;

  @Enumerated(EnumType.STRING)
  @Column(name = "furniture_type", nullable = false)
  private FurnitureType furnitureType;

  @Column(name = "is_visible", nullable = false)
  private Boolean isVisible;

  @ColumnDefault(value = "1")
  private int level;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public static Furniture createDefault(Room room, FurnitureType type) {
    return Furniture.builder()
        .room(room)
        .furnitureType(type)
        .level(1) // 기본 레벨 1
        .isVisible(false)
        .build();
  }


  @PrePersist
  protected void onCreate() {
    this.createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = LocalDateTime.now();
  }

  public static List<Furniture> createDefaultFurnitures(Room room, LocalDateTime now) {
    Furniture bookshelf = init(room, FurnitureType.BOOKSHELF, now);
    Furniture cdRack = init(room, FurnitureType.CD_RACK, now);
    return List.of(bookshelf, cdRack);
  }

  public static Furniture init(Room room, FurnitureType furnitureType, LocalDateTime now) {
    Furniture furniture = new Furniture();
    furniture.room = room;
    furniture.furnitureType = furnitureType;
    furniture.isVisible = false;
    furniture.level = 1;
    furniture.createdAt = now;
    furniture.updatedAt = now;
    return furniture;
  }

  public int getMaxCapacity() {
    return FurnitureCapacity.getCapacity(furnitureType, level);
  }

  public int getUpgradePrice() {
    return FurnitureUpgradePrice.getPrice(furnitureType, level);
  }

  public void setVisible(Boolean isVisible) {
    this.isVisible = isVisible;
  }

  public void upgradeLevel(int selectedLevel) {
    if (level == 3) {
      throw new BookshelfMaxLevelException();
    }
    if (selectedLevel - level != 1) {
      throw new BookshelfUpgradeDenyException();
    }
    User user = room.getUser();
    user.payPoints(FurnitureUpgradePrice.getPrice(furnitureType, level));
    level++;
  }
}
