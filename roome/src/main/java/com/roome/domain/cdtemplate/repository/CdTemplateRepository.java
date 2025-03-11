package com.roome.domain.cdtemplate.repository;

import com.roome.domain.cdtemplate.entity.CdTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CdTemplateRepository extends JpaRepository<CdTemplate, Long> {

  Optional<CdTemplate> findByMyCdId(Long myCdId);

  boolean existsByMyCdId(Long myCdId); // 존재 여부 확인
}
