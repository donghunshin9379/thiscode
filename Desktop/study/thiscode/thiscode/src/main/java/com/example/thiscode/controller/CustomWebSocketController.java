package com.example.thiscode.controller;

import com.example.thiscode.service.WebSocketService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Component
public class CustomWebSocketController extends TextWebSocketHandler {

    private final WebSocketService webSocketService;
    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketController.class);

    public CustomWebSocketController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }
    //3
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Principal principal = session.getPrincipal();
        if (principal != null) {
            String email = principal.getName();
            webSocketService.registerSession(session, email);
            
            // 본인 세션 등록 후 본인의 온라인친구 요청
            List<String> onlineFriends = webSocketService.getOnlineFriends(email);
            sendOnlineUsers(session, onlineFriends);
        } else {
            logger.warn("존재하지 않는 이메일 접근");
            session.close();
        }
    }
    
    // 요청 응답용
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JSONObject jsonObject = new JSONObject(message.getPayload());

            switch (jsonObject.getString("type")) {
                case "statusUpdate":
                    statusUpdate(jsonObject);
                    break;
                default:
                    logger.warn("알 수 없는 메시지 타입: {}", jsonObject.getString("type"));
            }
        } catch (JSONException e) {
            logger.error("메시지 처리 중 JSON 오류 발생", e);
        }
    }

    private void statusUpdate(JSONObject jsonObject) throws JSONException {
        JSONObject payload = jsonObject.getJSONObject("payload");
        String email = payload.getString("email");
        boolean isOnline = payload.getBoolean("isOnline");
        webSocketService.updateUserStatus(email, isOnline);

        // 해당 사용자의 온라인 친구 목록을 가져옴
        List<String> onlineFriends = webSocketService.getOnlineFriends(email);

        // 온라인 친구들에게 상태 업데이트 알림
        broadcastStatusUpdateToOnlineFriends(onlineFriends, email, isOnline);
    }
    
    //서버 TO 클라이언트
    private void sendOnlineUsers(WebSocketSession session, List<String> onlineFriends) {
        JSONObject payload = new JSONObject()
                .put("type", "onlineFriends")
                .put("friends", onlineFriends);

        try {
            session.sendMessage(new TextMessage(payload.toString()));
            logger.info("sendOnlineUsers 완료 - 세션 ID: {}", session.getId());
        } catch (IOException e) {
            logger.error("sendOnlineUsers 오류 발생", e);
        }
    }

    private void broadcastStatusUpdateToOnlineFriends(List<String> onlineFriends, String email, boolean isOnline) {
        JSONObject payload = new JSONObject()
                .put("type", "statusUpdate")
                .put("payload", new JSONObject()
                        .put("email", email)
                        .put("status", isOnline ? "online" : "offline"));

        for (String friendEmail : onlineFriends) {
            WebSocketSession friendSession = webSocketService.findSessionByEmail(friendEmail);
            if (friendSession != null && friendSession.isOpen()) { // 친구의 세션이 존재하고 온라인인 경우에만 메시지 전송
                try {
                    friendSession.sendMessage(new TextMessage(payload.toString()));
                } catch (IOException e) {
                    logger.error("Broadcast error to friend: {}", friendEmail, e);
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String email = (String) session.getAttributes().get("email");
        if (email != null) {
            webSocketService.updateUserStatus(email, false);
        }
        webSocketService.removeSession(session);
    }
}