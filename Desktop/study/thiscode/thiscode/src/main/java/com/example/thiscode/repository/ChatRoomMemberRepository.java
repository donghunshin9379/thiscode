package com.example.thiscode.repository;


import com.example.thiscode.domain.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {
    @Query("SELECT crm1.chatRoom.roomId FROM ChatRoomMember crm1 " +
            "JOIN ChatRoomMember crm2 ON crm1.chatRoom.roomId = crm2.chatRoom.roomId " +
            "WHERE crm1.userEmail = :user1Email AND crm2.userEmail = :user2Email")
    Long findCommonRoomId(@Param("user1Email") String user1Email, @Param("user2Email") String user2Email);

    @Modifying
    @Query("UPDATE ChatRoomMember m SET m.lastReadMessageId = :lastMessageId WHERE m.userEmail = :userEmail AND m.chatRoom.id = :roomId")
    void updateLastReadMessageId(@Param("userEmail") String userEmail, @Param("roomId") Long roomId, @Param("lastMessageId") Long lastMessageId);


}
