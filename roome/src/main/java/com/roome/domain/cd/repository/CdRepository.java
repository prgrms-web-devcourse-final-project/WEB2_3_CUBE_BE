package com.roome.domain.cd.repository;

import com.roome.domain.cd.entity.Cd;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CdRepository extends JpaRepository<Cd, Long> {

}
