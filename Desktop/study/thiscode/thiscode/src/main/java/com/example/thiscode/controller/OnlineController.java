package com.example.thiscode.controller;

import com.example.thiscode.service.WebSocketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/online")
public class OnlineController {

    private final WebSocketService webSocketService;

    @Autowired
    public OnlineController(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

}
