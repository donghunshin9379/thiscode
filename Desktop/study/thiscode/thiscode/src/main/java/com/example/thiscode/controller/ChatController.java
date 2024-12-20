package com.example.thiscode.controller;

import com.example.thiscode.domain.CustomUserDetails;
import com.example.thiscode.domain.Message;
import com.example.thiscode.domain.MessageRequest;
import com.example.thiscode.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // 채팅내역 불러오기 (GET 요청)
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getChatHistory(
            @RequestParam("friendEmail") String friendEmail,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        String currentUserEmail = userDetails.getUsername();
        Long roomId = chatService.getChatRoomId(currentUserEmail, friendEmail);
        List<Message> messages = chatService.getChatHistory(roomId);
        Map<String, Object> response = new HashMap<>();
        response.put("currentUserEmail", currentUserEmail);
        response.put("messages", messages);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest messageRequest, Principal principal) {
        String senderEmail = principal.getName();
        Long roomId = chatService.getChatRoomId(senderEmail, messageRequest.getReceiverEmail());
        Message savedMessage =
        chatService.saveMessage(roomId, senderEmail, messageRequest.getReceiverEmail(), messageRequest.getContent());
        return ResponseEntity.ok(savedMessage);
    }
}
