package com.roome.domain.houseMate.dto;

import com.roome.domain.user.entity.Status;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto {
    private Long userId;
    private Status status;
}