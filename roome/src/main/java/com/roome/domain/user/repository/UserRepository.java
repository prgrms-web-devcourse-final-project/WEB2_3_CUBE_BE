package com.roome.domain.user.repository;

import com.roome.domain.user.entity.User;
import com.roome.global.jwt.exception.UserNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    default User getById(Long id) {
        return findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    Optional<User> findByProviderId(String providerId);
}
