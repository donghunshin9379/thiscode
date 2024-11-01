package com.example.thiscode.controller;


import com.example.thiscode.domain.Message;
import com.example.thiscode.domain.MessageFile;
import com.example.thiscode.domain.MessageRoom;
import com.example.thiscode.service.MessageFileService;
import com.example.thiscode.service.MessageRoomService;
import com.example.thiscode.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final MessageFileService messageFileService;
    private final MessageRoomService messageRoomService;


    public MessageController(MessageService messageService, MessageFileService messageFileService, MessageRoomService messageRoomService) {
        this.messageService = messageService;
        this.messageFileService = messageFileService;
        this.messageRoomService = messageRoomService;
    }

    // 메시지 전송 API
    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        Message savedMessage = messageService.saveMessage(message);
        return ResponseEntity.ok(savedMessage);
    }

    // 특정 대화방의 메시지 조회 API
    @GetMapping("/{roomId}")
    public ResponseEntity<List<Message>> getMessages(@PathVariable Long roomId) {
        List<Message> messages = messageService.getMessagesByRoom(roomId);
        return ResponseEntity.ok(messages);
    }

    // 첨부파일 업로드 API
    @PostMapping("/files/upload")
    public ResponseEntity<MessageFile> uploadFile(@RequestBody MessageFile messageFile) {
        MessageFile savedFile = messageFileService.saveFile(messageFile);
        return ResponseEntity.ok(savedFile);
    }

    // 특정 메시지의 첨부파일 조회 API (예시)
    @GetMapping("/files/{messageId}")
    public ResponseEntity<List<MessageFile>> getFilesByMessageId(@PathVariable Long messageId) {
        List<MessageFile> files = messageFileService.getFilesByMessageId(messageId);
        return ResponseEntity.ok(files);
    }

    // 대화방 생성 API
    @PostMapping("/rooms")
    public ResponseEntity<MessageRoom> createRoom(@RequestBody MessageRoom messageRoom) {
        MessageRoom savedRoom = messageRoomService.createRoom(messageRoom);
        return ResponseEntity.ok(savedRoom);
    }

    // 대화방 ID로 대화방 정보 가져오기
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<MessageRoom> getRoom(@PathVariable Long roomId) {
        Optional<MessageRoom> roomOptional = messageRoomService.getRoomById(roomId);
        if (roomOptional.isPresent()) {
            return ResponseEntity.ok(roomOptional.get()); // 대화방이 존재하면 반환
        } else {
            return ResponseEntity.notFound().build(); // 대화방이 존재하지 않으면 404 응답
        }
    }
}
