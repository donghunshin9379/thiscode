package com.example.thiscode.service;

import com.example.thiscode.domain.Message;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class WebSocketService {
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> userOnlineFriends = new ConcurrentHashMap<>();
    private final FriendListService friendListService;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    public WebSocketService(FriendListService friendListService) {
        this.friendListService = friendListService;
    }

    // 이메일로 세션 조회
    public WebSocketSession findSessionByEmail(String email) {
        return userSessions.get(email); // 이메일로 세션을 직접 반환
    }

    //세션 등록
    public void registerSession(WebSocketSession session, String email) {
        if (email != null && !email.isEmpty()) {
            session.getAttributes().put("email", email);
            userSessions.put(email, session);
            initializeUserOnlineFriends(email);
            updateUserStatus(email, true);
        } else {
            logger.warn("유효한 이메일 없이 세션 등록 시도");
        }
    }

    //세션 제거
    public void removeSession(WebSocketSession session) {
        String email = (String) session.getAttributes().get("email");
        if (email != null) {
            updateUserStatus(email, false); // 유저상태 OFFLINE
            userSessions.remove(email);
            session.getAttributes().remove("email");
        } else {
            logger.warn("세션에서 유저 정보가 존재하지 않음");
        }

        try {
            if (session.isOpen()) {
                session.close();
            } else {
            }
        } catch (IOException e) {
            logger.error("세션 종료 중 오류 발생", e);
        }
    }

    // 온라인 친구 목록 조회
    public List<String> getOnlineFriends(String email) {
        Set<String> onlineFriends = userOnlineFriends.getOrDefault(email, new HashSet<>());
        return new ArrayList<>(onlineFriends);
    }

    // 유저 상태 업데이트
    public void updateUserStatus(String email, boolean isOnline) {
        if (isOnline) {
            addUserToFriendsOnlineList(email);
        } else {
            removeUserFromFriendsOnlineList(email);
        }
        // 유저 상태 업데이트 반영
        broadcastUserStatusToOnlineFriends(email, isOnline);
    }

    private void broadcastUserStatusToOnlineFriends(String email, boolean isOnline) {
        JSONObject payload = new JSONObject()
                .put("type", "statusUpdate")
                .put("payload", new JSONObject()
                        .put("email", email)
                        .put("status", isOnline ? "online" : "offline"));

        Set<String> friends = friendListService.getAllFriendEmails(email);
        for (String friendEmail : friends) {
            WebSocketSession friendSession = userSessions.get(friendEmail);
            if (friendSession != null && friendSession.isOpen()) {
                try {
                    friendSession.sendMessage(new TextMessage(payload.toString()));
                } catch (IOException e) {
                    logger.error("유저상태 업데이트 반영 실패 " + friendEmail, e);
                }
            }
        }
    }

    // 사용자의 온라인 친구 목록 초기화
    private void initializeUserOnlineFriends(String email) {
        Set<String> friends = friendListService.getAllFriendEmails(email);
        Set<String> onlineFriends = friends.stream()
                .filter(userSessions::containsKey)
                .collect(Collectors.toSet());
        userOnlineFriends.put(email, onlineFriends);
    }

    // 사용자를 친구들의 온라인 목록에 추가
    private void addUserToFriendsOnlineList(String email) {
        Set<String> friends = friendListService.getAllFriendEmails(email);
        for (String friendEmail : friends) {
            userOnlineFriends.computeIfPresent(friendEmail, (k, v) -> {
                v.add(email);
                return v;
            });
        }
    }

    // 사용자를 친구들의 온라인 목록에서 제거
    private void removeUserFromFriendsOnlineList(String email) {
        Set<String> friends = friendListService.getAllFriendEmails(email);
        for (String friendEmail : friends) {
            userOnlineFriends.computeIfPresent(friendEmail, (k, v) -> {
                v.remove(email);
                logger.info("친구 {}의 온라인 목록에서 {} 제거", friendEmail, email);
                return v;
            });
        }
        userOnlineFriends.remove(email);
    }

    // 입장 읽음 상태 업데이트를 알리는 메서드(발신자에게)
    public void sendReadStatusUpdate(Message message) {
        // 발신자의 세션을 찾습니다.
        WebSocketSession senderSession = findSessionByEmail(message.getSenderEmail());
        if (senderSession != null && senderSession.isOpen()) {
            JSONObject payload = new JSONObject()
                    .put("type", "readStatusUpdate")
                    .put("payload", new JSONObject()
                            .put("messageId", message.getId())
                            .put("isRead", true));
            try {
                senderSession.sendMessage(new TextMessage(payload.toString()));
                logger.info("발신자에게 읽음 상태 업데이트: {}", payload.toString());
            } catch (IOException e) {
                logger.error("발신자에게 읽음 상태 업데이트 전송 실패: {}", e.getMessage());
            }
        }
    }


}


