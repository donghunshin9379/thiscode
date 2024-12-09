package com.example.thiscode.service;


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
    
    // 이메일로 세션 찾기
    public WebSocketSession findSessionByEmail(String email) {
        return userSessions.get(email); // 이메일로 세션을 직접 반환
    }

    //세션 등록
    public void registerSession(WebSocketSession session, String email) {
        if (email != null && !email.isEmpty()) {
            logger.info("웹소켓 등록: {}", email);
            session.getAttributes().put("email", email);
            userSessions.put(email, session);
            initializeUserOnlineFriends(email);
            updateUserStatus(email, true);
            logger.info("현재 전체 온라인 유저: {}", userSessions.keySet());
        } else {
            logger.warn("유효한 이메일 없이 세션 등록 시도");
        }
    }

    //세션 제거
    public void removeSession(WebSocketSession session) {
        String email = (String) session.getAttributes().get("email");
        if (email != null) {
            // 사용자 상태를 오프라인으로 업데이트
            updateUserStatus(email, false);
            userSessions.remove(email);
            session.getAttributes().remove("email");
            logger.info("웹소켓 제거: {}", email);
        } else {
            logger.warn("세션에서 유저 정보가 존재하지 않음");
        }

        try {
            if (session.isOpen()) {
                session.close();
                logger.info("세션 정상 종료.");
            } else {
                logger.info("세션이 이미 종료되어있습니다.");
            }
        } catch (IOException e) {
            logger.error("세션 종료 중 오류 발생", e);
        }
    }

    // 온라인 친구 목록 가져오기
    public List<String> getOnlineFriends(String email) {
        Set<String> onlineFriends = userOnlineFriends.getOrDefault(email, new HashSet<>());

        logger.info(" {}의 온라인 친구: {}", email, onlineFriends);

        return new ArrayList<>(onlineFriends);
    }

    // 사용자 상태 업데이트
    public void updateUserStatus(String email, boolean isOnline) {
        if (isOnline) {
            logger.info("{} 유저 온라인", email);
            addUserToFriendsOnlineList(email);
        } else {
            removeUserFromFriendsOnlineList(email);
            logger.info("{} 유저 오프라인", email);
        }
        // 상태 변경 시 온라인 친구에게 알림
        broadcastUserStatusToOnlineFriends(email, isOnline);
        logger.info("전체 userOnlineFriends 상태: {}", userOnlineFriends);
    }

//    // 온라인 친구에게 사용자 상태 전송 #############기존###################
//    private void broadcastUserStatusToOnlineFriends(String email, boolean isOnline) {
//        JSONObject payload = new JSONObject()
//                .put("type", "statusUpdate")
//                .put("payload", new JSONObject()
//                        .put("email", email)
//                        .put("status", isOnline ? "online" : "offline"));
//
//        List<String> onlineFriends = getOnlineFriends(email); // 온라인 친구 목록 가져오기
//        for (String friendEmail : onlineFriends) { // 온라인 친구에게만 메시지 전송
//            WebSocketSession friendSession = userSessions.get(friendEmail);
//            if (friendSession != null && friendSession.isOpen()) {
//                try {
//                    friendSession.sendMessage(new TextMessage(payload.toString()));
//                } catch (IOException e) {
//                    logger.error("Error sending status update to friend: " + friendEmail, e);
//                }
//            }
//        }
//    }

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
                    logger.error("Error sending status update to friend: " + friendEmail, e);
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
        logger.info(" {}의 온라인 친구 초기화: {}", email, onlineFriends);
    }

    // 사용자를 친구들의 온라인 목록에 추가
    private void addUserToFriendsOnlineList(String email) {
        Set<String> friends = friendListService.getAllFriendEmails(email);
        for (String friendEmail : friends) {
            userOnlineFriends.computeIfPresent(friendEmail, (k, v) -> {
                v.add(email);
                logger.info("친구 {}의 온라인 목록에 {} 추가", friendEmail, email);
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

}


