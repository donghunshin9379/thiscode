package com.example.thiscode.repository;

import com.example.thiscode.domain.Message;
import com.example.thiscode.domain.MessageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageFileRepository extends JpaRepository<MessageFile, Long> {
    // 메시지 ID로 첨부파일 조회
    List<MessageFile> findByMessage_Id(Long messageId);
}
