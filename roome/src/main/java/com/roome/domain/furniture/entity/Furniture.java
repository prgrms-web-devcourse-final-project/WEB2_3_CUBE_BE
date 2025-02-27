package com.roome.domain.furniture.entity;

import com.roome.domain.furniture.exception.BookshelfMaxLevelException;
import com.roome.domain.furniture.exception.BookshelfUpgradeDenyException;
import com.roome.domain.room.entity.Room;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

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
    private Room room;

    @Enumerated(EnumType.STRING)
    @Column(name = "furniture_type", nullable = false)
    private FurnitureType furnitureType;

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    @ColumnDefault(value = "1")
    private int level = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public int getMaxCapacity(){
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
