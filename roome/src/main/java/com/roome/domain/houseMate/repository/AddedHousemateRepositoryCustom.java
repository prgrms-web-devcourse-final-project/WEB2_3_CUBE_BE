package com.roome.domain.houseMate.repository;

import com.roome.domain.houseMate.dto.HousemateInfo;

import java.util.List;

public interface AddedHousemateRepositoryCustom {
    List<HousemateInfo> findByUserId(Long userId, String cursor, int limit, String nickname);
    List<HousemateInfo> findByAddedId(Long addedId, String cursor, int limit, String nickname);
}