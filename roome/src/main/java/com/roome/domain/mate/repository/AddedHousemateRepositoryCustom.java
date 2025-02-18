package com.roome.domain.mate.repository;

import com.roome.domain.mate.dto.HousemateInfo;

import java.util.List;

public interface AddedHousemateRepositoryCustom {
    List<HousemateInfo> findByUserId(Long userId, String cursor, int limit, String nickname);
    List<HousemateInfo> findByAddedId(Long addedId, String cursor, int limit, String nickname);
}