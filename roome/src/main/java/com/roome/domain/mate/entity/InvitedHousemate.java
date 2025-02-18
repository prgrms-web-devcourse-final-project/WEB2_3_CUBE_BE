package com.roome.domain.mate.entity;

import com.roome.domain.user.entity.User;
import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "invited_housemates")
public class InvitedHousemate extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @JoinColumn(name = "user_id")
    private Long userId;

    @JoinColumn(name = "invited_id")
    private Long InvitedId;

    @Builder
    public InvitedHousemate(Long userId, Long invited) {
        this.userId = userId;
        this.InvitedId = invited;
    }
}