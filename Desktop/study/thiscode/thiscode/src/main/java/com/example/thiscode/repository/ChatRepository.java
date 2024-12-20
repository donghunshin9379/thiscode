package com.example.thiscode.repository;

import com.example.thiscode.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// 채팅 메세지 저장 + 조회
public interface ChatRepository extends JpaRepository<Message, Long> {
    // roomId 모든 메시지 조회
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId ORDER BY m.createdAt ASC")
    List<Message> findAllByRoomIdOrderByCreatedAtAsc(@Param("roomId") Long roomId);

    // 메시지 배치 저장 (성능 최적화)
    @Modifying
    @Query(value = "INSERT INTO messages (room_id, sender_id, content, created_at, is_read) VALUES :messages", nativeQuery = true)
    void batchInsert(@Param("messages") List<Object[]> messages);

}