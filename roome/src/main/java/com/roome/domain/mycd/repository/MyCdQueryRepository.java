package com.roome.domain.mycd.repository;

import com.roome.domain.mycd.entity.MyCd;
import org.springframework.data.domain.Page;

public interface MyCdQueryRepository {

  Page<MyCd> searchMyCd(Long userId, String keyword, Long cursor, int size);
}
