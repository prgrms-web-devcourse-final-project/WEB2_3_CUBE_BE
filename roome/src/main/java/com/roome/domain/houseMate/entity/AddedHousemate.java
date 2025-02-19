package com.roome.domain.houseMate.entity;

import com.roome.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "added_housemates")
public class AddedHousemate extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "user_id")
    private Long userId;

    @JoinColumn(name = "added_id")
    private Long addedId;

    @Builder
    public AddedHousemate(Long userId, Long addedId) {
        this.userId = userId;
        this.addedId = addedId;
    }
}
