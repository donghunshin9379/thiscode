package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "messages")
@Getter
@Setter
public class Message {

    //자동id값 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 메시지 고유 ID

    @ManyToOne
    @JoinColumn(name = "sender_email", nullable = false)
    private Member sender; // 발신자

    @ManyToOne
    @JoinColumn(name = "receiver_email", nullable = false)
    private Member receiver; // 수신자

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private MessageRoom room; // 대화방

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 메시지 내용

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now(); // 전송 시간

    @Column(nullable = false)
    private boolean isRead = false; // 메시지 읽음 여부
}
