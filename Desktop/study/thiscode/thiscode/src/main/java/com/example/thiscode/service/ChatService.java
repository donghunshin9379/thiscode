package com.example.thiscode.service;

import com.example.thiscode.domain.ChatRoom;
import com.example.thiscode.domain.Message;
import com.example.thiscode.repository.ChatRepository;
import com.example.thiscode.repository.ChatRoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
@Transactional
public class ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final WebSocketService webSocketService;
    private final Logger logger = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatRepository chatRepository, ChatRoomRepository chatRoomRepository, WebSocketService webSocketService) {
        this.chatRepository = chatRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.webSocketService = webSocketService;
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
        message.setSenderEmail(senderEmail);
        message.setReceiverEmail(receiverEmail);
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(Boolean.FALSE);

        return chatRepository.save(message);
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

    public void updateMessageReadStatus(Message message) {
        message.setIsRead(true);
        chatRepository.save(message);
    }

    //본인 속한 채팅방 GET
    public List<String> getChatFriendsList(String userEmail) {
        return chatRepository.findDistinctFriendsWithContent(userEmail);
    }

    public void markMessagesAsRead(String userEmail, Long roomId) {
        // 읽지 않은 메시지 목록
        List<Message> unreadMessages = chatRepository.findUnreadMessages(userEmail, roomId);
        for (Message message : unreadMessages) {
            message.setIsRead(true);
            chatRepository.save(message);
            webSocketService.sendReadStatusUpdate(message);
        }
    }

}
