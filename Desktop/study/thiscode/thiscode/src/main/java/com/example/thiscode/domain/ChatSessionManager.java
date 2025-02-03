package com.example.thiscode.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatSessionManager {
    // 사용자 ID를 키로, 현재 접속 중인 채팅방 ID를 값으로 하는 맵
    private static final Map<String, Long> userRooms = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ChatSessionManager.class);


    public void removeUserRoom(String userEmail, Long roomId) {
        userRooms.remove(userEmail, roomId);
        // 필요한 경우 추가적인 정리 작업 수행
    }

    public void setUserRoom(String userEmail, Long roomId) {
        userRooms.put(userEmail, roomId);
    }

    public boolean isUserInRoom(String userEmail, Long roomId) {
        logger.info("isUserInRoom  userEmail : {}",userEmail);
        logger.info("isUserInRoom  roomId : {}", roomId);
        Long currentRoomId = userRooms.get(userEmail);
        return currentRoomId != null && currentRoomId.equals(roomId);
    }

    public Long getUserRoom(String userEmail) {
        return userRooms.get(userEmail);
    }
}

