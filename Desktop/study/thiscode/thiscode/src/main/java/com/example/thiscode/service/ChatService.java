package com.example.thiscode.service;

import com.example.thiscode.domain.ChatRoom;
import com.example.thiscode.domain.Message;
import com.example.thiscode.repository.ChatRepository;
import com.example.thiscode.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
    }

    //채팅내역 get
    public List<Message> getChatHistory(Long roomId) {
        return chatRepository.findAllByRoomIdOrderByCreatedAtAsc(roomId);
    }

    // 단일 메시지 저장 (실시간용)
    public Message saveMessage(Long roomId, String senderEmail, String receiverEmail, String content) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        Message message = new Message();
        message.setRoomId(roomId);
        message.setSenderEmail(senderEmail); // senderId 대신 senderEmail 사용
        message.setReceiverEmail(receiverEmail); // 수신자 이메일 설정
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        message.setRead(false);

        return chatRepository.save(message);
    }

    // 여러 메시지 저장 (전체 조회용)
    public void saveMessages(List<Message> messages) {
        List<Object[]> messageData = messages.stream()
                .map(m -> new Object[]{m.getRoomId(), m.getSenderEmail(), m.getReceiverEmail(), m.getContent(), m.getCreatedAt(), m.isRead()})
                .collect(Collectors.toList());
        chatRepository.batchInsert(messageData);
    }

    public Long getChatRoomId(String user1Email, String user2Email) {
        // 이메일을 정렬하여 항상 같은 순서로 조회
        String[] sortedEmails = new String[]{user1Email, user2Email};
        Arrays.sort(sortedEmails);

        ChatRoom chatRoom = chatRoomRepository.findByUser1EmailAndUser2Email(sortedEmails[0], sortedEmails[1]);
        if (chatRoom == null) {
            chatRoom = new ChatRoom();
            chatRoom.setUser1Email(sortedEmails[0]);
            chatRoom.setUser2Email(sortedEmails[1]);
            chatRoom = chatRoomRepository.save(chatRoom);
        }
        return chatRoom.getRoomId();
    }


    private ChatRoom createChatRoom(String user1Email, String user2Email) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setUser1Email(user1Email);
        chatRoom.setUser2Email(user2Email);
        return chatRoomRepository.save(chatRoom);
    }

// 다이렉트메세지에 활용가능
//    public List<ChatRoom> getChatRoomsForUser(String userEmail) {
//        List<ChatRoom> chatRooms = chatRoomRepository.findByUser1EmailOrUser2Email(userEmail, userEmail);
//
//        // 채팅 내역이 있는 방만 필터링 (선택적)
//        return chatRooms.stream()
//                .filter(room -> chatRepository.existsByRoomId(room.getId()))
//                .collect(Collectors.toList());
//    }
}
