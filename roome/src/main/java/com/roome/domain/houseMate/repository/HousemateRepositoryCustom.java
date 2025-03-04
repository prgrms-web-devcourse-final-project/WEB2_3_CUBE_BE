package com.roome.domain.houseMate.repository;

import com.roome.domain.houseMate.dto.HousemateInfo;

import java.util.List;

public interface HousemateRepositoryCustom {
    List<HousemateInfo> findByUserId(Long userId, Long cursor, int limit, String nickname);
    List<HousemateInfo> findByAddedId(Long addedId, Long cursor, int limit, String nickname);
    List<Long> findFollowerIdsByAddedId(Long addedId);
    List<Long> findFollowingIdsByUserId(Long userId);
}