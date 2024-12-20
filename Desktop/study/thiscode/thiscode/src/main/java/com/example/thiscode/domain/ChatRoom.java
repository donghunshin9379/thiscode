package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms",
uniqueConstraints = {@UniqueConstraint(columnNames = {"user1_email", "user2_email"})})
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "user1_email", nullable = false)
    private String user1Email;

    @Column(name = "user2_email", nullable = false)
    private String user2Email;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public ChatRoom(String user1Email, String user2Email) {
        this.user1Email = user1Email;
        this.user2Email = user2Email;
        this.createdAt = LocalDateTime.now();
    }
}