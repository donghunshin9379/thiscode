package com.example.thiscode.handler;

import com.example.thiscode.service.WebSocketService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.security.Principal;

// 프론트 socket 객체 수신용
@Component
public class CustomWebSocketHandler extends TextWebSocketHandler {

    private final WebSocketService webSocketService;
    private static final Logger logger = LoggerFactory.getLogger(CustomWebSocketHandler.class);

    public CustomWebSocketHandler( WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    // 웹소켓 연결 후 해당 유저정보 저장 > 등록 (후속 작업에 활용)
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 세션에서 인증 정보 확인
        Principal principal = session.getPrincipal();
        if (principal != null) {
            String email = principal.getName();
            logger.info("CustomeWebSocketHandler 이메일 : {}", email);
            webSocketService.registerSession(session, email);
        } else {
            // 인증되지 않은 연결 처리
            session.close();
        }
    }
    
    // 프론트 socket.send'getOnlineUsers'
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        JSONObject jsonObject = new JSONObject(payload);

        if ("getOnlineUsers".equals(jsonObject.getString("type"))) {
            webSocketService.sendOnlineUsers(session);
        }
    }

    // 연결 종료 후
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        webSocketService.removeSession(session);
            logger.info("웹소켓 : 유저 정보가 삭제되었습니다. ");
    }

}

