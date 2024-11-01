package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message_room")
@Getter
@Setter
public class MessageRoom {

    //자동id값 생성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 대화방 고유 ID

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Message> messages = new ArrayList<>(); // 대화방의 메시지 목록

    @OneToMany(mappedBy = "messageRoom", cascade = CascadeType.ALL)
    private List<RoomParticipants> participants = new ArrayList<>(); // 대화방 참여자 목록
}
