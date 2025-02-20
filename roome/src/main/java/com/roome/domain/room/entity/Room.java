package com.roome.domain.room.entity;

import com.roome.domain.furniture.entity.Furniture;
import com.roome.domain.furniture.entity.FurnitureType;
import com.roome.domain.room.exception.RoomAuthorizationException;
import com.roome.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
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
    private RoomTheme theme;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Furniture> furnitures = new ArrayList<>();

    public int getMaxMusic(){
        return furnitures.stream()
                .filter(f -> f.getFurnitureType() == FurnitureType.CD_RACK)
                .mapToInt(Furniture::getMaxCapacity)
                .sum();
    }

    public int getMaxBooks(){
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
}
