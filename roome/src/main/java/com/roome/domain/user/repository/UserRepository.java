package com.roome.domain.user.repository;

import com.roome.domain.user.entity.User;
import com.roome.global.jwt.exception.UserNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    default User getById(Long id) {
        return findById(id)
                .orElseThrow(UserNotFoundException::new);
    }

    Optional<User> findByProviderId(String providerId);

    Optional<User> findByEmail(String email);

    @Transactional
    @Modifying
    @Query(
            value = """
                update users set last_login = :now where id = :userId
                """,
            nativeQuery = true
    )
    void updateLastLogin(Long userId, LocalDateTime now);

    List<User> findByIdNot(Long id);
}
