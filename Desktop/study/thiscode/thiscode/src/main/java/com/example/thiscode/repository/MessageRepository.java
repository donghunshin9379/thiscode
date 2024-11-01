package com.example.thiscode.repository;

import com.example.thiscode.domain.FriendList;
import com.example.thiscode.domain.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

}
