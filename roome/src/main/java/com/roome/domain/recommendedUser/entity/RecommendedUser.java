package com.roome.domain.recommendedUser.entity;

import com.roome.domain.user.entity.User;
import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recommended_users",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "recommended_user_id"}))
public class RecommendedUser extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_user_id", nullable = false)
    private User recommendedUser;

    @Column(nullable = false)
    private Integer similarityScore;

    public static RecommendedUser create(User user, User recommendedUser, Integer similarityScore) {
        return RecommendedUser.builder()
                .user(user)
                .recommendedUser(recommendedUser)
                .similarityScore(similarityScore)
                .build();
    }
}
