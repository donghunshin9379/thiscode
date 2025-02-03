package com.example.thiscode.service;

import com.example.thiscode.domain.ChatRoom;
import com.example.thiscode.domain.ChatRoomMember;
import com.example.thiscode.domain.Message;
import com.example.thiscode.repository.ChatRoomMemberRepository;
import com.example.thiscode.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class ChatRoomService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;

    public ChatRoomService(ChatRoomRepository chatRoomRepository, ChatRoomMemberRepository chatRoomMemberRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRoomMemberRepository = chatRoomMemberRepository;
    }

    public Long getChatRoomId(String user1Email, String user2Email) {
        Long commonRoomId = chatRoomMemberRepository.findCommonRoomId(user1Email, user2Email);

        if (commonRoomId != null) {
            return commonRoomId;
        }

        // user1Email 및 user2Email 매개변수를 사용하여 ChatRoom 객체 생성
        ChatRoom newChatRoom = chatRoomRepository.save(new ChatRoom(user1Email, user2Email));

        chatRoomMemberRepository.save(new ChatRoomMember(newChatRoom, user1Email, null, LocalDateTime.now()));
        chatRoomMemberRepository.save(new ChatRoomMember(newChatRoom, user2Email, null, LocalDateTime.now()));

        return newChatRoom.getRoomId();
    }
}

