package com.roome.domain.point.repository;

import com.roome.domain.point.entity.Point;
import com.roome.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

  Optional<Point> findByUser(User user);

  Optional<Point> findByUserId(Long userId);
}
