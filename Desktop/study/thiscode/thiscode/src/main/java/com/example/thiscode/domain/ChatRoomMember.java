package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private String userEmail;

    private Long lastReadMessageId;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Builder
    public ChatRoomMember(ChatRoom chatRoom, String userEmail, Long lastReadMessageId, LocalDateTime joinedAt) {
        this.chatRoom = chatRoom;
        this.userEmail = userEmail;
        this.lastReadMessageId = lastReadMessageId;
        this.joinedAt = joinedAt;
    }
}