package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend_request",
        uniqueConstraints = @UniqueConstraint(columnNames =
        {"requester_username", "recipient_username"})) // 유일성 제약 조건 추가
@Getter
@Setter
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "requester_username", nullable = false)
    private String requesterUsername;

    @Column(name = "recipient_username", nullable = false)
    private String recipientUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;



    // 기본 생성자
    public FriendRequest() {
        this.createdAt = LocalDateTime.now(); // 생성 시각 자동 설정
        this.status = FriendStatus.PENDING; // 기본 상태 설정
    }

    // 생성자
    public FriendRequest(String requesterUsername, String recipientUsername) {
        this.requesterUsername = requesterUsername;
        this.recipientUsername = recipientUsername;
        this.createdAt = LocalDateTime.now(); // 생성 시각 자동 설정
        this.status = FriendStatus.PENDING; // 기본 상태 설정
    }

    // 상태 업데이트
    public void acceptRequest() {
        this.status = FriendStatus.ACCEPTED;
        this.createdAt = LocalDateTime.now(); // 수정 시각 업데이트
    }

    public void blockRequest() {
        this.status = FriendStatus.BLOCKED;
        this.createdAt = LocalDateTime.now(); // 수정 시각 업데이트
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "requesterUsername='" + requesterUsername + '\'' +
                ", recipientUsername='" + recipientUsername + '\'' +
                ", status=" + status +
                '}';
    }

}


