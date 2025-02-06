package com.example.thiscode.controller;

import com.example.thiscode.domain.ChatSessionManager;
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
    private final ChatSessionManager chatSessionManager;

    public CustomWebSocketController(WebSocketService webSocketService, ChatService chatService, ChatSessionManager chatSessionManager) {
        this.webSocketService = webSocketService;
        this.chatService = chatService;
        this.chatSessionManager = chatSessionManager;
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
                case "enterRoom" :
                    enterRoom(jsonObject, session);
                    break;
                case "leaveRoom" :
                    leaveRoom(jsonObject, session);
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


//    private void sendMessage(JSONObject jsonObject, WebSocketSession session) throws JSONException {
//        JSONObject payload = jsonObject.getJSONObject("payload");
//        String receiverEmail = payload.getString("receiverEmail");
//        String content = payload.getString("content");
//
//
//        Principal principal = session.getPrincipal();
//        String senderEmail = principal.getName();
//
//        // 메시지 저장
//        Long roomId = chatService.getChatRoomId(senderEmail, receiverEmail);
//        Message savedMessage = chatService.saveMessage(roomId, senderEmail, receiverEmail, content);
//
//        //친구에게 메시지 전송
//        sendChatMessageToFriend(savedMessage, receiverEmail);
//    }
private void sendMessage(JSONObject jsonObject, WebSocketSession session) throws JSONException {
    JSONObject payload = jsonObject.getJSONObject("payload");
    String receiverEmail = payload.getString("receiverEmail");
    String content = payload.getString("content");

    Principal principal = session.getPrincipal();
    String senderEmail = principal.getName();

    // 메시지 저장
    Long roomId = chatService.getChatRoomId(senderEmail, receiverEmail);
    Message savedMessage = chatService.saveMessage(roomId, senderEmail, receiverEmail, content);

    // 친구에게 메시지 전송 및 읽음 상태 확인
    boolean isRead = sendChatMessageToFriend(savedMessage, receiverEmail);

    // 클라이언트에게 응답 전송
    JSONObject response = new JSONObject()
            .put("type", "messageSent")
            .put("payload", new JSONObject()
                    .put("messageId", savedMessage.getId())
                    .put("isRead", isRead)
                    .put("content", content));

    try {

        session.sendMessage(new TextMessage(response.toString()));
    } catch (IOException e) {
        logger.error("응답 메시지 전송 오류: {}", senderEmail, e);
    }
}

    // 메시지 전송 및 읽음 상태 업데이트
    private boolean sendChatMessageToFriend(Message message, String receiverEmail) {
        logger.info("웹소켓 컨트롤러 sendChatMessageToFriend 실행");
        JSONObject payload = new JSONObject()
                .put("type", "displayMessage")
                .put("payload", new JSONObject()
                        .put("senderEmail", message.getSenderEmail())
                        .put("content", message.getContent()));

        logger.info("sendChatMessageToFriend sender 이메일값 : {}", message.getSenderEmail());
        // 친구의 세션을 찾기
        WebSocketSession friendSession = webSocketService.findSessionByEmail(receiverEmail);
        boolean isRead = false;

        // 수신자가 채팅방에 있는지 확인 후 읽음 상태 저장 및 브로드캐스트
        if (friendSession != null && isFriendInRoom(receiverEmail, message.getRoomId(), message)) {
            chatService.updateMessageReadStatus(message);  // 읽음 상태 업데이트
            broadcastReadStatusUpdateToSession(message.getId(), true, friendSession);  // 읽음 상태 브로드캐스트
            isRead = true;
        }

        if (friendSession != null) {
            // 친구의 세션이 존재하면 메시지를 전송합니다.
            try {
                friendSession.sendMessage(new TextMessage(payload.toString()));
                logger.info("메시지 전송 완료 - 수신자: {}", receiverEmail);
            } catch (IOException e) {
                logger.error("메시지 전송 오류: {}", receiverEmail, e);
            }
        } else {
            logger.warn("수신자 {} 세션 존재 X.", receiverEmail);

        }

        return isRead;
    }

    // 유저 채팅방 입장 여부 확인 (boolean 리턴)
    private boolean isFriendInRoom(String receiverEmail, Long roomId, Message message) {
        boolean isReceiverInRoom = chatSessionManager.isUserInRoom(receiverEmail, roomId);
        if (!isReceiverInRoom) {
            logger.warn("수신자가 채팅방에 있지 않습니다: {}", receiverEmail);
            // 수신자가 채팅방이 아닌 경우 알림 전송
            sendNotification(receiverEmail, message);
        }
        return isReceiverInRoom;
    }

    // 알림 전송 메소드
    private void sendNotification(String receiverEmail, Message message) {
        JSONObject notificationPayload = new JSONObject()
                .put("type", "notification")
                .put("payload", new JSONObject()
                        .put("messageId", message.getId())
                        .put("senderEmail", message.getSenderEmail())
                        .put("content", message.getContent()));


        // 친구의 세션을 찾기
        WebSocketSession friendSession = webSocketService.findSessionByEmail(receiverEmail);

        if (friendSession != null) {
            try {
                friendSession.sendMessage(new TextMessage(notificationPayload.toString()));
                logger.info("알림 전송 완료 - 수신자: {}", receiverEmail);
                logger.info("알림 완료 발신자 : {}", message.getSenderEmail());
            } catch (IOException e) {
                logger.error("알림 전송 오류: {}", receiverEmail, e);
            }
        } else {
            logger.warn("수신자 {} 세션 존재 X.", receiverEmail);
        }
    }




    // 특정 세션에 읽음 상태 업데이트 브로드캐스트
    private void broadcastReadStatusUpdateToSession(Long messageId, boolean isRead, WebSocketSession session) {
        JSONObject payload = new JSONObject()
                .put("type", "readStatusUpdate")
                .put("payload", new JSONObject()
                        .put("messageId", messageId)
                        .put("isRead", isRead));

        if (session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(payload.toString()));
            } catch (IOException e) {
                logger.error("읽음 상태 업데이트 전송 실패: {}", e.getMessage());
            }
        }
    }

//    //메시지 전송 및 읽음 상태 업데이트
//    private void sendChatMessageToFriend(Message message, String receiverEmail) {
//        logger.info("웹소켓 컨트롤러 sendChatMessageToFriend 실행");
//        JSONObject payload = new JSONObject()
//                .put("type", "chatMessage")
//                .put("payload", new JSONObject()
//                        .put("senderEmail", message.getSenderEmail())
//                        .put("content", message.getContent()));
//
//        // 수신자가 채팅방에 있는지 확인
//        isFriendInRoom(message, receiverEmail, message.getRoomId());
//        // 친구의 세션을 찾기
//        WebSocketSession friendSession = webSocketService.findSessionByEmail(receiverEmail);
//
//        if (friendSession != null) {
//            // 친구의 세션이 존재하면 메시지를 전송합니다.
//            try {
//                friendSession.sendMessage(new TextMessage(payload.toString()));
//                logger.info("메시지 전송 완료 - 수신자: {}", receiverEmail);
//            } catch (IOException e) {
//                logger.error("메시지 전송 오류: {}", receiverEmail, e);
//            }
//        } else {
//            logger.warn("수신자 {}는 오프라인입니다. 메시지는 이미 데이터베이스에 저장되어 있습니다.", receiverEmail);
//        }
//    }


//    // 유저 채팅방 입장 여부로 실시간 읽음 처리
//    private void isFriendInRoom(Message message, String receiverEmail, Long roomId) {
//        boolean isReceiverInRoom = chatSessionManager.isUserInRoom(receiverEmail, roomId);
//
//        // 상대가 채팅방에 있는 경우
//        if (isReceiverInRoom) {
//            chatService.updateMessageReadStatus(message);
//        } else {
//            logger.warn("수신자가 채팅방에 있지 않습니다: {}", receiverEmail);
//        }
//    }




    private void enterRoom(JSONObject jsonObject, WebSocketSession session) throws JSONException {
        Long roomId = jsonObject.getLong("roomId");
        String userEmail = (String) session.getAttributes().get("email");
        //유저 채팅방 위치 유지 및 확인 (실시간 빌드업)
        chatSessionManager.setUserRoom(userEmail, roomId);

        //입장시 안 읽은 메세지 읽음처리로직 (DB)
        chatService.markMessagesAsRead(userEmail, roomId);
    }

    private void leaveRoom(JSONObject jsonObject, WebSocketSession session) throws JSONException {
        Long roomId = jsonObject.getLong("roomId");
        String userEmail = (String) session.getAttributes().get("email");
        logger.info("WebSocketController leaveRoom - roomId : {}", roomId);
        logger.info("WebSocketController leaveRoom - userEmail : {}", userEmail);

        // 유저를 채팅방에서 제거
        chatSessionManager.removeUserRoom(userEmail, roomId);
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