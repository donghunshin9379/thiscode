package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
// 메세지 Entity
@Entity
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "sender_email", nullable = false) // sender_id를 sender_email로 변경
    private String senderEmail;

    @Column(name = "receiver_email", nullable = false) // 수신자 이메일 추가
    private String receiverEmail;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    public Message(Long roomId, String senderEmail, String receiverEmail, String content) {
        this.roomId = roomId;
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
    }
}
