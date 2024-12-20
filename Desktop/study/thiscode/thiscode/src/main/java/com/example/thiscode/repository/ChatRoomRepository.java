package com.example.thiscode.repository;

import com.example.thiscode.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 두 사용자의 이메일로 채팅방을 찾는 메소드
    ChatRoom findByUser1EmailAndUser2Email(String user1Email, String user2Email);

}
