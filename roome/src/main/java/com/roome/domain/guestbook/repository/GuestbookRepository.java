package com.roome.domain.guestbook.repository;

import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.room.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {
    Page<Guestbook> findByRoom(Room room, Pageable pageable);
}