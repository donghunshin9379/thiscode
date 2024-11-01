package com.example.thiscode.service;

import com.example.thiscode.domain.Message;
import com.example.thiscode.domain.MessageFile;
import com.example.thiscode.domain.MessageRoom;
import com.example.thiscode.repository.MessageFileRepository;
import com.example.thiscode.repository.MessageRoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageRoomService {

    private final MessageRoomRepository messageRoomRepository;
    private final MessageService messageService;
    
    public MessageRoomService(MessageRoomRepository messageRoomRepository, MessageService messageService) {
        this.messageRoomRepository = messageRoomRepository;
        this.messageService = messageService;
    }

    // 대화방 생성
    public MessageRoom createRoom(MessageRoom room) {
        return messageRoomRepository.save(room);
    }

    // 대화방 ID로 대화방 정보 가져오기
    public Optional<MessageRoom> getRoomById(Long roomId) {
        return messageRoomRepository.findById(roomId);
    }

    // 특정 대화방의 모든 메시지 조회 (메시지 서비스와 연결하여 사용)
    public List<Message> getMessagesInRoom(Long roomId) {
        // 이 메소드는 MessageService의 메소드를 호출하여 메시지를 가져옴
        return messageService.getMessagesByRoom(roomId); // MessageService 주입 필요
    }
}