package com.example.thiscode.service;

import com.example.thiscode.domain.Member;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketService {
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    private final MemberService memberService;
    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    public WebSocketService(MemberService memberService) {
        this.memberService = memberService;
    }

    // 이메일로 세션 찾기
    public WebSocketSession findSessionByEmail(String email) {
        logger.info("웹소켓서비스 findSessionByEmail : {}",email);
        return userSessions.get(email); // 이메일로 세션을 직접 반환
    }

    // 세션에서 이메일을 추출
    private String getEmailFromSession(WebSocketSession session) {
        return (String) session.getAttributes().get("email");
    }

    // 세션 등록
    public void registerSession(WebSocketSession session, String email) {
        if (email != null && !email.isEmpty()) {
            session.getAttributes().put("email", email);
            userSessions.put(email, session);
            broadcastUserStatus(email, true);
            logger.info("등록된 웹소켓 유저 세션 - 이메일: {}", email);
            logger.info("등록된 웹소켓 유저 세션 - 세션: {}", session);
        } else {
            logger.warn("유효한 이메일 없이 세션 등록 시도");
        }
    }

//    public void removeSession(WebSocketSession session) {
//        // 세션에서 유저 정보를 가져옴
//        Optional<Member> memberInfoOpt =
//        Optional.ofNullable((Member) session.getAttributes().get("memberInfo"));
//        logger.info("웹소켓서비스 removeSession 실행 {}", memberInfoOpt);
//
//        // 유저 정보가 존재할 경우에만 처리
//        memberInfoOpt.ifPresent(memberInfo -> {
//            String email = memberInfo.getEmail();
//
//            // userSessions에서 세션 제거
//            userSessions.remove(email);
//
//            // 오프라인 상태로 방송
//            broadcastUserStatus(email, false); // false로 설정하여 오프라인 상태 방송
//
//            logger.info("제거된 웹소켓 유저 세션 {}", memberInfo);
//        });
//
//        // 세션 종료 처리
//        try {
//            session.close(); // 웹소켓 세션 종료
//        } catch (IOException e) {
//            logger.error("세션 종료 중 오류 발생", e);
//        }
//    }

    public void removeSession(WebSocketSession session) {
        // 세션에서 유저 정보를 가져옴 (이메일을 가져옴)
        String email = (String) session.getAttributes().get("email"); // "email" 키로 이메일을 가져옴
        logger.info("웹소켓서비스 removeSession 실행 - 이메일: {}", email);

        if (email != null) { // 이메일이 null이 아닐 경우 처리
            // userSessions에서 세션 제거
            userSessions.remove(email);

            // 오프라인 상태로 방송
            broadcastUserStatus(email, false); // false로 설정하여 오프라인 상태 방송

            logger.info("제거된 웹소켓 유저 세션 - 이메일: {}", email);
        } else {
            logger.warn("세션에서 유저 정보가 존재하지 않음");
        }

        // 세션 종료 처리
        try {
            session.close(); // 웹소켓 세션 종료
        } catch (IOException e) {
            logger.error("세션 종료 중 오류 발생", e);
        }
    }
    public void broadcastUserStatus(String email, boolean isOnline) {
        // 이메일을 사용하여 업데이트 처리
        JSONObject payload = new JSONObject()
                .put("email", email)
                .put("status", isOnline ? "online" : "offline");

        broadcastJsonMessage("statusUpdate", payload);

        // 오프라인 상태일 때만 세션 제거
        if (!isOnline) {
            userSessions.remove(email);
        }
    }

    private void broadcastJsonMessage(String type, JSONObject payload) {
        JSONObject message = new JSONObject()
                .put("type", type)
                .put("payload", payload);

        String messageString = message.toString();
        for (WebSocketSession session : userSessions.values()) {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(messageString));
                }
            } catch (IOException e) {
                logger.error("Error sending message to session: {}", e.getMessage());
            }
        }
    }

    // 온라인 유저
    public void sendOnlineUsers(WebSocketSession session) {
        logger.info("WebSocketService sendOnlineUsers 실행");
        try {
            JSONObject response = new JSONObject();
            JSONArray usersArray = new JSONArray();

            String currentUserEmail = getEmailFromSession(session);

            for (String email : userSessions.keySet()) {
                // 현재 사용자를 제외
                if (!email.equals(currentUserEmail)) {
                    JSONObject userInfo = getUserInfo(email);
                    usersArray.put(userInfo);
                }
            }

            response.put("type", "onlineUsers");
            response.put("users", usersArray);

            session.sendMessage(new TextMessage(response.toString()));

            // 개선된 로그 메시지
            logger.info("온라인 사용자 목록 전송 완료 - 수신자: {}, 세션 ID: {}, 온라인 사용자 수: {}",
                    currentUserEmail, session.getId(), usersArray.length());
        } catch (IOException e) {
            logger.error("온라인 사용자 목록 전송 실패 - 세션 ID: {}", session.getId(), e);
        } catch (JSONException e) {
            logger.error("온라인 사용자 JSON 응답 생성 오류", e);
        }
    }

    // 사용자 정보를 조회하는 메서드
    private JSONObject getUserInfo(String email) {
        Optional<Member> memberOpt = memberService.getUserInfo(email);
        if (memberOpt.isPresent()) {
            Member member = memberOpt.get();
            return new JSONObject()
                    .put("email", member.getEmail())
                    .put("username", member.getUsername())
                    .put("nickname", member.getNickname())
                    .put("localDate", member.getLocalDate().toString());
        }
        return new JSONObject().put("email", email);
    }

}