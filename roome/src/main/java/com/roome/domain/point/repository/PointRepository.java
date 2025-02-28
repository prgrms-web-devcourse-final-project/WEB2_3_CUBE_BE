package com.roome.domain.point.repository;

import com.roome.domain.point.entity.Point;
import com.roome.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface PointRepository extends JpaRepository<Point, Long> {

  Optional<Point> findByUser(User user);
}
