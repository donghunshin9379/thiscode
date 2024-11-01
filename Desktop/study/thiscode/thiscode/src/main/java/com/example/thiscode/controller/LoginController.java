package com.example.thiscode.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("me")
    public String home() {
        return "me";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
