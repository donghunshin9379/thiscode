package com.example.thiscode.service;

import com.example.thiscode.domain.Message;
import com.example.thiscode.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // 메시지 저장
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    // 메시지 목록 가져오기
    public List<Message> getMessagesByRoom(Long roomId) {
        // 특정 대화방의 메시지 조회 로직
        return messageRepository.findAll(); // 예시
    }

    // 기타 필요한 메소드
}