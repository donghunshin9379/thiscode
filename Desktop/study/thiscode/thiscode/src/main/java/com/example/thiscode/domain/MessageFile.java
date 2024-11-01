package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "message_file")
@Getter
@Setter
public class MessageFile {

    //자동id값 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 첨부 파일 고유 ID

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message; // 첨부된 메시지

    @Column(nullable = false)
    private String filePath; // 파일 저장 경로 또는 URL

    @Column(nullable = false)
    private String fileName; // 원본 파일 이름

    @Column(nullable = false)
    private String fileType; // 파일 타입

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now(); // 업로드 시간

}
