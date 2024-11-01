package com.example.thiscode.service;

import com.example.thiscode.domain.Message;
import com.example.thiscode.domain.MessageFile;
import com.example.thiscode.repository.MessageFileRepository;
import com.example.thiscode.repository.MessageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageFileService {

    private final MessageFileRepository messageFileRepository;

    public MessageFileService(MessageFileRepository messageFileRepository) {
        this.messageFileRepository = messageFileRepository;
    }

    // 첨부파일 저장
    public MessageFile saveFile(MessageFile file) {
        return messageFileRepository.save(file);
    }

    // 메시지 ID로 첨부파일 조회
    public List<MessageFile> getFilesByMessageId(Long messageId) {
        return messageFileRepository.findByMessage_Id(messageId);
    }
}