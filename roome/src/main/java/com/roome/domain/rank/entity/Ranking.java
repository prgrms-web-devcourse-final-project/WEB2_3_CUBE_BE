package com.roome.domain.rank.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "ranking")
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false)
    private int score = 0;

    @Column(name = "rank_position", nullable = false)
    private int rankPosition = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    public void addScore(int points) {
        this.score += points;
        this.lastUpdated = LocalDateTime.now();
    }

    public void resetScore() {
        this.score = 0;
        this.lastUpdated = LocalDateTime.now();
    }
}
