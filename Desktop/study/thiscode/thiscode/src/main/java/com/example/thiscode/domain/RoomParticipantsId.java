package com.example.thiscode.domain;

import java.io.Serializable;
import java.util.Objects;

public class RoomParticipantsId implements Serializable {
    private final Long roomId;
    private final String participantEmail;

    public RoomParticipantsId(Long roomId, String participantEmail) {
        this.roomId = roomId;
        this.participantEmail = participantEmail;
    }


    // equals()와 hashCode() 메서드 오버라이드
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoomParticipantsId)) return false;
        RoomParticipantsId that = (RoomParticipantsId) o;
        return Objects.equals(roomId, that.roomId) && Objects.equals(participantEmail, that.participantEmail);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, participantEmail);
    }
}
