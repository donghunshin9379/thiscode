package com.example.thiscode.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "room_participants")
@IdClass(RoomParticipantsId.class) // 복합 키를 위한 IdClass 지정
@Getter
@Setter
public class RoomParticipants {

    @Id
    @Column(name = "room_id")
    private Long roomId;

    @Id
    @Column(name = "participant_email")
    private String participantEmail;

    @ManyToOne
    @JoinColumn(name = "room_id", insertable = false, updatable = false)
    private MessageRoom messageRoom; // 대화방과의 관계
}
