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

    //DM 리스트 (채팅내역존재) 목록
    @Query("SELECT DISTINCT CASE " +
            "WHEN m.senderEmail = :userEmail THEN m.receiverEmail " +
            "ELSE m.senderEmail END " +
            "FROM Message m " +
            "WHERE (m.senderEmail = :userEmail OR m.receiverEmail = :userEmail) " +
            "AND m.content IS NOT NULL AND m.content <> ''")
    List<String> findDistinctFriendsWithContent(@Param("userEmail") String userEmail);

    @Query("SELECT m FROM Message m WHERE m.receiverEmail = :userEmail AND m.roomId = :roomId AND m.isRead = false")
    List<Message> findUnreadMessages(@Param("userEmail") String userEmail, @Param("roomId") Long roomId);


}