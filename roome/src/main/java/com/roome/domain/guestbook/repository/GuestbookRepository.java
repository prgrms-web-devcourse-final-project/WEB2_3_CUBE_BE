package com.roome.domain.guestbook.repository;

import com.roome.domain.guestbook.entity.Guestbook;
import com.roome.domain.guestbook.entity.RelationType;
import com.roome.domain.room.entity.Room;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuestbookRepository extends JpaRepository<Guestbook, Long> {

  Page<Guestbook> findByRoom(Room room, Pageable pageable);

  // 특정 방의 방명록이거나 특정 사용자가 작성한 방명록 목록 조회
  @Query("SELECT g FROM Guestbook g WHERE g.room = :room OR g.user.id = :userId")
  List<Guestbook> findAllByRoomOrUserId(@Param("room") Room room, @Param("userId") Long userId);

  List<Guestbook> findAllByUserId(Long userId);

  @Query("SELECT DISTINCT g.user.id FROM Guestbook g WHERE g.room.id = :roomId")
  List<Long> findAllUserIdsByRoomId(@Param("roomId") Long roomId);

  @Modifying
  @Query("UPDATE Guestbook g SET g.relation = :relation WHERE g.user.id = :guestId AND g.room.user.id = :ownerId")
  void updateRelationType(@Param("guestId") Long guestId, @Param("ownerId") Long ownerId, @Param("relation") RelationType relation);
}