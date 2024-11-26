package com.example.thiscode.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {
    // 로그인
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    
    // 로그인 후
    @GetMapping("/friends")
    public String friends() {
        return "friends";
    }
}
