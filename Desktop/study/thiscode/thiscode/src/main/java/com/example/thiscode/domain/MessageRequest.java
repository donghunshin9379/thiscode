package com.example.thiscode.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequest {
    private String receiverEmail;
    private String content;

}