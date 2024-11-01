package com.example.thiscode.repository;

import com.example.thiscode.domain.MessageFile;
import com.example.thiscode.domain.MessageRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRoomRepository extends JpaRepository<MessageRoom, Long> {

}
