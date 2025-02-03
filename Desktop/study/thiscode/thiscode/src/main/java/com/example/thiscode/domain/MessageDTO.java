package com.example.thiscode.domain;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Getter
@Setter
public class MessageDTO {
    private Long id;
    private String senderEmail;
    private String receiverEmail;
    private String content;
    private Long roomId;
    private LocalDateTime createdAt;
    private boolean isRead;
}
