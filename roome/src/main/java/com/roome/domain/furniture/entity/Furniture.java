package com.roome.domain.furniture.entity;

import com.roome.domain.room.entity.Room;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private int level;

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
        switch(level){
            case 1:
                return 5;
            case 2:
                return 10;
            case 3:
                return 15;
            default:
                throw new IllegalStateException("유효하지 않은 가구 레벨입니다.");
        }
    }
}
