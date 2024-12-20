package com.example.thiscode.controller;

import com.example.thiscode.domain.Message;
import com.example.thiscode.service.ChatService;
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
    private final ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketController.class);

    public CustomWebSocketController(WebSocketService webSocketService, ChatService chatService) {
        this.webSocketService = webSocketService;
        this.chatService = chatService;
    }

    //소켓 생성후 온라인 유저 요청(탭X)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Principal principal = session.getPrincipal();
        if (principal != null) {
            String email = principal.getName();
            webSocketService.registerSession(session, email);

            // 본인 시점 이전 온라인친구 로드
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
                // 온라인 친구 탭 응답
                case "onlineFriends":
                    Principal principal = session.getPrincipal();
                    String email = principal.getName();
                    List<String> onlineFriends = webSocketService.getOnlineFriends(email);
                    sendOnlineUsers(session, onlineFriends);
                    break;
                // 채팅 응답용
                case "sendMessage":
                    sendMessage(jsonObject, session);
                    break;
                default:
                    logger.warn("알 수 없는 메시지 타입: {}", jsonObject.getString("type"));
            }
        } catch (JSONException e) {
            logger.error("메시지 처리 중 JSON 오류 발생", e);
        }
    }

    //서버 TO 클라이언트
    private void sendOnlineUsers(WebSocketSession session, List<String> onlineFriends) {
        JSONObject payload = new JSONObject()
                .put("type", "onlineFriends")
                .put("friends", onlineFriends);
        try {
            session.sendMessage(new TextMessage(payload.toString()));
        } catch (IOException e) {
            logger.error("sendOnlineUsers 오류 발생", e);
        }
    }

    private void statusUpdate(JSONObject jsonObject) throws JSONException {
        JSONObject payload = jsonObject.getJSONObject("payload");
        String email = payload.getString("email");
        boolean isOnline = payload.getBoolean("isOnline");
        webSocketService.updateUserStatus(email, isOnline);
        List<String> onlineFriends = webSocketService.getOnlineFriends(email);
        broadcastStatusUpdateToOnlineFriends(onlineFriends, email, isOnline);
    }

    private void broadcastStatusUpdateToOnlineFriends(List<String> onlineFriends, String email, boolean isOnline) {
        JSONObject payload = new JSONObject()
                .put("type", "statusUpdate")
                .put("payload", new JSONObject()
                .put("email", email)
                .put("status", isOnline ? "online" : "offline"));

        for (String friendEmail : onlineFriends) {
            WebSocketSession friendSession = webSocketService.findSessionByEmail(friendEmail);
            if (friendSession != null && friendSession.isOpen()) {
                try {
                    friendSession.sendMessage(new TextMessage(payload.toString()));
                } catch (IOException e) {
                    logger.error("Broadcast error to friend: {}", friendEmail, e);
                }
            }
        }
    }


    private void sendMessage(JSONObject jsonObject, WebSocketSession session) throws JSONException {
        JSONObject payload = jsonObject.getJSONObject("payload");
        String receiverEmail = payload.getString("receiverEmail");
        String content = payload.getString("content");


        Principal principal = session.getPrincipal();
        String senderEmail = principal.getName();

        // 메시지 저장
        Long roomId = chatService.getChatRoomId(senderEmail, receiverEmail);
        Message savedMessage = chatService.saveMessage(roomId, senderEmail, receiverEmail, content);

        //친구에게 메시지 전송
        sendChatMessageToFriend(savedMessage, receiverEmail);
    }

    private void sendChatMessageToFriend(Message message, String receiverEmail) {
        logger.info("웹소켓 컨트롤러 sendChatMessageToFriend 실행");
        JSONObject payload = new JSONObject()
                .put("type", "chatMessage")
                .put("payload", new JSONObject()
                        .put("senderEmail", message.getSenderEmail())
                        .put("content", message.getContent()));

        // 친구의 세션을 찾습니다.
        WebSocketSession friendSession = webSocketService.findSessionByEmail(receiverEmail);

        if (friendSession != null) {
            // 친구의 세션이 존재하면 메시지를 전송합니다.
            try {
                friendSession.sendMessage(new TextMessage(payload.toString()));
                logger.info("메시지 전송 완료 - 수신자: {}", receiverEmail);
            } catch (IOException e) {
                logger.error("메시지 전송 오류: {}", receiverEmail, e);
            }
        } else {
            logger.warn("수신자 {}는 오프라인입니다. 메시지는 이미 데이터베이스에 저장되어 있습니다.", receiverEmail);
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